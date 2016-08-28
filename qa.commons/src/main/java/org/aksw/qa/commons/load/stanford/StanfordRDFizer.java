package org.aksw.qa.commons.load.stanford;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.iterator.Filter;
import org.apache.jena.vocabulary.RDFS;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.StopURIsSKOS;
import org.dllearner.algorithms.qtl.util.filters.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SymmetricConciseBoundedDescriptionGeneratorImpl;
import org.json.simple.parser.ParseException;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

//TODO actually refactor this class to an own submodule
public class StanfordRDFizer {

	private AGDISTIS disambiguator;
	private Spotlight recognizer;
	private DBpediaIndex index;

	public StanfordRDFizer() {
		this.disambiguator = new AGDISTIS();
		this.recognizer = new Spotlight();
		this.index = new DBpediaIndex();
	}

	public Map<String, List<Entity>> recognize(String question) {
		return this.recognizer.getEntities(question);
	}

	public String disambiguate(String label) {

		String preAnnotatedText = "<entity>" + label + "</entity>";

		HashMap<String, String> results;
		try {
			results = disambiguator.runDisambiguation(preAnnotatedText);
			for (String namedEntity : results.keySet()) {
				return results.get(namedEntity);
			}
		} catch (ParseException | IOException e) {
			// TODO build in proper logging
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		// DBpedia as SPARQL endpoint
		QueryExecutionFactory qef = FluentQueryExecutionFactory.http("http://dbpedia.org/sparql", Lists.newArrayList("http://dbpedia.org")).config()
		        .withPostProcessor(qe -> ((QueryEngineHTTP) ((QueryExecutionHttpWrapper) qe).getDecoratee()).setModelContentType(WebContent.contentTypeRDFXML)).end().create();
		// CBD generator
		ConciseBoundedDescriptionGenerator cbdGen = new ConciseBoundedDescriptionGeneratorImpl(qef);
		cbdGen = new SymmetricConciseBoundedDescriptionGeneratorImpl(qef);
		cbdGen.setRecursionDepth(1);
		// query tree factory
		QueryTreeFactory qtf = new QueryTreeFactoryBase();
		// filters
		ArrayList<Filter<Statement>> treeFilters = Lists.newArrayList(new PredicateDropStatementFilter(StopURIsDBpedia.get()), new ObjectDropStatementFilter(StopURIsDBpedia.get()),
		        new PredicateDropStatementFilter(Sets.union(StopURIsRDFS.get(), Sets.newHashSet(RDFS.seeAlso.getURI()))), new PredicateDropStatementFilter(StopURIsOWL.get()),
		        new ObjectDropStatementFilter(StopURIsOWL.get()), new PredicateDropStatementFilter(StopURIsSKOS.get()), new ObjectDropStatementFilter(StopURIsSKOS.get()),
		        new NamespaceDropStatementFilter(Sets.newHashSet("http://dbpedia.org/property/", "http://purl.org/dc/terms/", "http://dbpedia.org/class/yago/", FOAF.getURI())),
		        new PredicateDropStatementFilter(Sets.newHashSet("http://www.w3.org/2002/07/owl#equivalentClass", "http://www.w3.org/2002/07/owl#disjointWith")));
		qtf.addDropFilters((Filter<Statement>[]) treeFilters.toArray(new Filter[treeFilters.size()]));
		// LGG generator
		LGGGenerator lggGen = new LGGGeneratorSimple();

		StanfordRDFizer stan = new StanfordRDFizer();

		// 1) NOTE! Each question has normally exactly one answer, but if
		// crowd-workers disagreed they can have multiple answers

		Dataset[] datasets = { Dataset.Stanford_dev };

		for (Dataset dataset : datasets) {
			List<IQuestion> questions = LoaderController.load(Dataset.Stanford_dev);

			BufferedWriter bw = new BufferedWriter(new FileWriter(datasets.toString().toLowerCase() + ".tsv"));

			// 2) run linking over all answers using AGDISTIS
			int numberOfLinkableAnswers = 0;
			int numberOfQuestions = 0;
			for (IQuestion q : questions) {
				String question = q.getLanguageToQuestion().get("en");
				// if (q.getGoldenAnswers().size() == 1) {

				Set<String> disambiguatedAnswers = new HashSet<>();

				for (String answer : q.getGoldenAnswers()) {
					String disambiguate = stan.disambiguate(answer);
					System.out.println("Number of Questions: " + numberOfQuestions + ". Question: " + question);
					numberOfQuestions++;
					bw.write(question + "\t");
					if (disambiguate != null) {
						numberOfLinkableAnswers++;
						System.out.println("\tDisambiguated Answer: " + answer + " -> " + disambiguate);
						bw.write(answer + "\t");
						bw.write(disambiguate + "\t");
						disambiguatedAnswers.add(disambiguate);
					} else {
						bw.write(answer + "\t");
						bw.write("null" + "\t");
					}
					// 3) run NER+NED over all questions, according to
					// experiments, we use Spotlight
					System.out.print("\tRecognized Entities in Question: ");
					Map<String, List<Entity>> recognize = stan.recognize(question);
					if (!recognize.isEmpty()) {
						recognize.get("en").forEach(x -> {
							try {
								bw.write(x.uris.get(0) + "\t");
							} catch (Exception e) {
								e.printStackTrace();
							}
							System.out.print(x.uris.get(0) + "\t");
						});
						System.out.println("\n");
					}

					// 4) run class and property detection over query
					System.out.print("\tRecognized Classes and Properties in Question: ");
					Arrays.stream(question.replaceAll("[^\\w]", " ").replaceAll("[\\d]", "").replaceAll("  ", " ").split(" ")).forEach(y -> {
						ArrayList<String> classOrProperty = stan.index.search(y);
						if (!classOrProperty.isEmpty()) {
							classOrProperty.forEach(x -> {
								try {
									bw.write(x + "\t");
								} catch (Exception e) {
									e.printStackTrace();
								}
								System.out.print(x + "\t");
							});
							System.out.println();
						}
					});
					System.out.println("\n");

					// 5) for queries with a resource as answer run QTL
					int minNrOfExamples = 2;
					if (disambiguatedAnswers.size() >= minNrOfExamples) {
						List<RDFResourceTree> trees = new ArrayList<>(disambiguatedAnswers.size());
						for (String uri : disambiguatedAnswers) {
							// generate CBD
							Model cbd = cbdGen.getConciseBoundedDescription(uri);
							System.out.println("|cbd(" + uri + ")|=" + cbd.size() + " triples");
							// generate query tree
							RDFResourceTree tree = qtf.getQueryTree(uri, cbd);
							trees.add(tree);
							// System.out.println(tree.getStringRepresentation(true));
						}

						// compute LGG
						RDFResourceTree lgg = lggGen.getLGG(trees);

						// SPARQL query
						Query query = QueryTreeUtils.toSPARQLQuery(lgg);

						// 5) run best QTL against DBpedia and measure
						// f-measure/accuracy to answer
						System.out.println(query);
					}

					bw.newLine();
				}
				// }
				if (numberOfQuestions % 50 == 0) {
					bw.flush();
				}
			}
			bw.close();
			System.out.println("Number Of Linkable Answers " + numberOfLinkableAnswers);
		}
	}
}
