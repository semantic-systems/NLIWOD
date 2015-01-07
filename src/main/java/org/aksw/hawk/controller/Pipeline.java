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
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Pipeline {
	static Logger log = LoggerFactory.getLogger(Pipeline.class);
	String dataset;
	QALD_Loader datasetLoader;
	public ASpotter nerdModule;
	public CachedParseTree cParseTree;
	public MutableTreePruner pruner;
	public SentenceToSequence sentenceToSequence;
	public Annotater annotater;
	public SPARQLQueryBuilder queryBuilder;

	void run() throws IOException {
		// 1. read in Questions from QALD 4
		List<Question> questions = datasetLoader.load(dataset);
		double overallf = 0;
		double overallp = 0;
		double overallr = 0;
		double counter = 0;
		Set<EvalObj> evals = Sets.newHashSet();
		for (Question q : questions) {
			if (q.answerType.equals("resource")) {
				if (q.onlydbo) {
					if (!q.aggregation) {
						String question = q.languageToQuestion.get("en");
						Map<String, Set<RDFNode>> answer = calculateSPARQLRepresentation(q);

						double fmax = 0;
						double pmax = 0;
						double rmax = 0;
						for (String query : answer.keySet()) {
							Set<RDFNode> systemAnswers = answer.get(query);
							// 11. Compare to set of resources from benchmark
							double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
							double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
							double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
							if (fMeasure > fmax) {
								log.info(query.toString());
								log.info("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
								fmax = fMeasure;
								pmax = precision;
								rmax = recall;
							}
						}
						evals.add(new EvalObj(question, fmax, pmax, rmax, "Assuming Optimal Ranking Function, Spotter: " + nerdModule.toString()));
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
//			break;
		}
		write(evals);
		log.info("Average P=" + overallp / counter + " R=" + overallr / counter + " F=" + overallf / counter + " Counter=" + counter);
	}

	private void write(Set<EvalObj> evals) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("results.html"));
			bw.write("<script src=\"sorttable.js\"></script><table class=\"sortable\">");
			bw.newLine();
			bw.write(" <tr>     <th>Question</th><th>F-measure</th><th>Precision</th><th>Recall</th><th>Comment</th>  </tr>");
			for (EvalObj eval : evals) {
				bw.write(" <tr>    <td>" + eval.getQuestion() + "</td><td>" + eval.getFmax() + "</td><td>" + eval.getPmax() + "</td><td>" + eval.getRmax() + "</td><td>" + eval.getComment() + "</td>  </tr>");
				bw.newLine();
			}
			bw.write("</table>");
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	public Map<String, Set<RDFNode>> calculateSPARQLRepresentation(Question q) {
		log.info(q.languageToQuestion.get("en"));
		// 2. Disambiguate parts of the query
		q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));

		// 3. Build trees from questions and cache them
		q.tree = cParseTree.process(q);
		// noun combiner, decrease #nodes in the DEPTree decreases
		sentenceToSequence.combineSequences(q);

		// 4. Apply pruning rules
		q.tree = pruner.prune(q);

		// 5. Annotate tree
		annotater.annotateTree(q);
		log.info(q.tree.toString());

		// 6. Build queries via subqueries
		Map<String, Set<RDFNode>> answer = queryBuilder.build(q);
		return answer;
	}

	public static void main(String args[]) throws IOException {
		
		for (String file : new String[] { "resources/qald-4_hybrid_train.xml" , "resources/qald-4_hybrid_test_withanswers.xml" }) { // test_withanswers train 
			Pipeline controller = new Pipeline();
//		
			log.info("Configuring controller");

			controller.dataset = new File(file).getAbsolutePath();
			controller.datasetLoader = new QALD_Loader();
			// ASpotter wiki = new WikipediaMiner();
			// controller.nerdModule = new MultiSpotter(fox, tagMe, wiki, spot);
			controller.nerdModule = new Fox();
			// controller.nerdModule = new Spotlight();
			// controller.nerdModule =new TagMe();
			// controller.nerdModule = new WikipediaMiner();

			controller.cParseTree = new CachedParseTree();

			controller.sentenceToSequence = new SentenceToSequence();
			
			controller.annotater = new Annotater();

			SPARQL sparql = new SPARQL();
			controller.queryBuilder = new SPARQLQueryBuilder(sparql);

			controller.pruner = new MutableTreePruner();
			log.info("Run controller");
			controller.run();
		}
	}
}