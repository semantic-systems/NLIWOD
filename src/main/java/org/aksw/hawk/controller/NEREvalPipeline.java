package org.aksw.hawk.controller;

import java.io.File;
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
import org.aksw.hawk.nlp.spotter.MultiSpotter;
import org.aksw.hawk.nlp.spotter.OptimalAnnotator;
import org.aksw.hawk.nlp.spotter.Spotlight;
import org.aksw.hawk.nlp.spotter.TagMe;
import org.aksw.hawk.nlp.spotter.WikipediaMiner;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQueryBuilder;
import org.aksw.hawk.ranking.VotingBasedRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class NEREvalPipeline {
	static Logger log = LoggerFactory.getLogger(NEREvalPipeline.class);
	private QALD_Loader datasetLoader;
	private ASpotter nerdModule;
	private CachedParseTree cParseTree;
	private SentenceToSequence sentenceToSequence;
	private MutableTreePruner pruner;
	private Annotater annotater;
	private SPARQLQueryBuilder queryBuilder;
	private VotingBasedRanker ranker;
	private String dataset;
	private Cardinality cardinality;

	public NEREvalPipeline() {
		datasetLoader = new QALD_Loader();
		cardinality = new Cardinality();
		cParseTree = new CachedParseTree();
		sentenceToSequence = new SentenceToSequence();
		pruner = new MutableTreePruner();
		ranker = new VotingBasedRanker();

		SPARQL sparql = new SPARQL();
		annotater = new Annotater(sparql);
		queryBuilder = new SPARQLQueryBuilder(sparql);
	}

	void run() throws IOException {
		log.info("QuestionId\tPrecision\tRecall\tF-measure\tSpotter");
		for (ASpotter nerdModule : Lists.newArrayList(new OptimalAnnotator())) {

//		for (ASpotter nerdModule : Lists.newArrayList(new TagMe(),new Fox(),  new WikipediaMiner(), new Spotlight(), new MultiSpotter(new Fox(), new TagMe(), new WikipediaMiner(), new Spotlight()))) {
			List<Question> questions = datasetLoader.load(dataset);
			this.nerdModule = nerdModule;
			double overallf = 0;
			double overallp = 0;
			double overallr = 0;
			double counter = 0;
			for (Question q : questions) {
				if (q.answerType.equals("resource")) {
					if (q.onlydbo) {
						if (!q.aggregation) {
							Map<String, Answer> answer = calculateSPARQLRepresentation(q);

							double fmax = 0;
							double pmax = 0;
							double rmax = 0;
							for (String query : answer.keySet()) {
								Set<RDFNode> systemAnswers = answer.get(query).answerSet;
								// 11. Compare to set of resources from
								// benchmark
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
									}
									fmax = fMeasure;
									pmax = precision;
									rmax = recall;
								}
							}
							log.info(q.id + "\t" + pmax + "\t" + rmax + "\t" + fmax + "\t" + nerdModule.toString());

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
			log.debug(overallp / counter + "\t" + overallr / counter + "\t" + overallf / counter + "\t" + nerdModule.toString());
		}
	}

	public static void main(String args[]) throws IOException {
		// #######################
		// Calculated the influence of spotting with optimal ranker
		// #######################
		log.info("Configuring controller");
		NEREvalPipeline controller = new NEREvalPipeline();

		for (String file : new String[] { "resources/qald-4_hybrid_train.xml" }) { // test_withanswers
			controller.dataset = new File(file).getAbsolutePath();
			log.info("Run controller");
			controller.run();
		}

	}

	public Map<String, Answer> calculateSPARQLRepresentation(Question q) {
		log.info(q.languageToQuestion.get("en"));
		// 2. Disambiguate parts of the query
		q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));

		// 3. Build trees from questions and cache them
		q.tree = cParseTree.process(q);
		log.info("" + q.tree);

		q.cardinality = cardinality.cardinality(q);
		// noun combiner, decrease #nodes in the DEPTree decreases
		sentenceToSequence.combineSequences(q);

		// 4. Apply pruning rules
		q.tree = pruner.prune(q);

		// 5. Annotate tree
		annotater.annotateTree(q);
		log.info(q.tree.toString());

		Map<String, Answer> answer = queryBuilder.build(q, ranker);
		return answer;
	}

}