package org.aksw.hawk.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.cache.CachedParseTree;
import org.aksw.hawk.nlp.MutableTreePruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.hawk.querybuilding.SPARQLQueryBuilder;
import org.aksw.hawk.ranking.VotingBasedRanker;
import org.aksw.hawk.ranking.VotingBasedRanker.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class TrainPipeline {
	static Logger log = LoggerFactory.getLogger(TrainPipeline.class);
	private QALD_Loader datasetLoader;
	private Fox nerdModule;
	private CachedParseTree cParseTree;
	private SentenceToSequence sentenceToSequence;
	private MutableTreePruner pruner;
	private Annotater annotater;
	private SPARQLQueryBuilder queryBuilder;
	private VotingBasedRanker ranker;
	private String dataset;
	private Cardinality cardinality;

	public TrainPipeline() {

		datasetLoader = new QALD_Loader();

		cardinality = new Cardinality();
		// ASpotter wiki = new WikipediaMiner();
		// controller.nerdModule = new MultiSpotter(fox, tagMe, wiki, spot);
		nerdModule = new Fox();
		// controller.nerdModule = new Spotlight();
		// controller.nerdModule =new TagMe();
		// controller.nerdModule = new WikipediaMiner();

		cParseTree = new CachedParseTree();

		sentenceToSequence = new SentenceToSequence();

		pruner = new MutableTreePruner();

		SPARQL sparql = new SPARQL();
		annotater = new Annotater(sparql);
		queryBuilder = new SPARQLQueryBuilder(sparql);

		ranker = new VotingBasedRanker();

	}

	void run(Set<EvalObj> evals, Set<Feature> featureSet) throws IOException {
		// read in Questions from QALD 4
		List<Question> questions = datasetLoader.load(dataset);
		double overallf = 0;
		double overallp = 0;
		double overallr = 0;
		double counter = 0;
		for (Question q : questions) {
			if (q.hybrid) {
				if (q.id == 301) {
					if (q.answerType.equals("resource")) {
						if (q.onlydbo) {
							if (!q.aggregation) {
								String question = q.languageToQuestion.get("en");
								Map<String, Answer> answer = calculateSPARQLRepresentation(q, featureSet);

								double fmax = 0;
								double pmax = 0;
								double rmax = 0;
								Set<SPARQLQuery> correctQueries = Sets.newHashSet();
								// Compare to set of resources from benchmark
								for (String query : answer.keySet()) {
									Set<RDFNode> systemAnswers = answer.get(query).answerSet;
									double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
									double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
									double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
									if (fMeasure >= fmax && fMeasure > 0) {
										log.debug(query.toString());
										log.debug("P=" + precision + " R=" + recall + " F=" + fMeasure);
										if (fMeasure > fmax) {
											// used if query with score of 0.4
											// is in set and a new one with 0.6
											// comes into save only worthy
											// queries with constant f-measure
											correctQueries.clear();
										}
										fmax = fMeasure;
										pmax = precision;
										rmax = recall;
										correctQueries.add(answer.get(query).query);
									}
								}
								this.ranker.learn(q, correctQueries);
								evals.add(new EvalObj(q.id, question, fmax, pmax, rmax, "Assuming Optimal Ranking Function, Spotter: " + nerdModule.toString()));
								overallf += fmax;
								overallp += pmax;
								overallr += rmax;
								counter++;
								log.info("########################################################");
							} else {
								// evals.add(new EvalObj(question,0, 0, 0,
								// "This question askes for aggregation (ASK)"));
							}
						} else {
							// evals.add(new EvalObj(question,0, 0, 0,
							// "This question askes for yago types"));
						}
					} else {
						// evals.add(new EvalObj(question,0, 0, 0,
						// "This is no question asking for resources only"));
					}
					// break;
				}
			}
		}
		log.debug("Features: " + featureSet);
		log.debug("Average P=" + overallp / counter + " R=" + overallr / counter + " F=" + overallf / counter + " Counter=" + counter);
		// log.info("F@n\t" + count + "\tF=" + +overallf / counter + "\t" +
		// ranker.getFeatures() + "\t" + dataset);
		// }
	}

	public static void main(String args[]) throws IOException {

		Set<EvalObj> evals = Sets.newHashSet();

		log.info("Configuring controller");
		TrainPipeline controller = new TrainPipeline();

		log.info("Run controller");
		// for (String file : new String[] { "resources/qald-4_hybrid_train.xml"
		// }) { // test_withanswers
		for (String file : new String[] { "resources/qald-5_train.xml", "resources/qald-5_test_questions.xml" }) {
			controller.dataset = new File(file).getAbsolutePath();
			controller.run(evals, null);
		}
		log.info("Writing results");
		controller.write(evals);
	}

	public Map<String, Answer> calculateSPARQLRepresentation(Question q, Set<Feature> featureSet) {
		log.info(q.languageToQuestion.get("en"));
		// 2. Disambiguate parts of the query
		q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));

		// noun combiner, decrease #nodes in the DEPTree
		sentenceToSequence.combineSequences(q);

		// 3. Build trees from questions and cache them
		q.tree = cParseTree.process(q);
		log.info("" + q.tree);

		// Cardinality identifies the integer i used for LIMIT i
		q.cardinality = cardinality.cardinality(q);

		// 4. Apply pruning rules
		q.tree = pruner.prune(q);

		// 5. Annotate tree
		annotater.annotateTree(q);
		log.info(q.tree.toString());
		// Map<String, Answer> answer = Maps.newHashMap();
		Map<String, Answer> answer = queryBuilder.build(q, ranker);
		return answer;
	}

	protected void write(Set<EvalObj> evals) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("results.html"));
			bw.write("<script src=\"sorttable.js\"></script><table class=\"sortable\">");
			bw.newLine();
			bw.write(" <tr>     <th>id</th><th>Question</th><th>F-measure</th><th>Precision</th><th>Recall</th><th>Comment</th>  </tr>");
			for (EvalObj eval : evals) {
				bw.write(" <tr>    <td>" + eval.getId() + "</td><td>" + eval.getQuestion() + "</td><td>" + eval.getFmax() + "</td><td>" + eval.getPmax() + "</td><td>" + eval.getRmax() + "</td><td>" + eval.getComment() + "</td>  </tr>");
				bw.newLine();
			}
			bw.write("</table>");
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

}