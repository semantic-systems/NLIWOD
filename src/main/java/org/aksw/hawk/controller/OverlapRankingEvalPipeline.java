package org.aksw.hawk.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.cache.CachedParseTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.hawk.nlp.MutableTreePruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQueryBuilder;
import org.aksw.hawk.ranking.OverlapBucketRanker;
import org.aksw.hawk.ranking.VotingBasedRanker;
import org.aksw.hawk.ranking.VotingBasedRanker.Feature;
import org.aksw.hawk.util.QALDWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class OverlapRankingEvalPipeline {
	static Logger log = LoggerFactory.getLogger(OverlapRankingEvalPipeline.class);
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
	private QALDWriter qw;


	public OverlapRankingEvalPipeline() {

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

	void run() throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		List<Question> questions = datasetLoader.load(dataset);
		qw = new QALDWriter(dataset);
		double overallf = 0;
		double overallp = 0;
		double overallr = 0;
		double counter = 0;
		for (Question q : questions) {
			if (q.hybrid) {
				if (q.answerType.equals("resource")) {
					if (q.onlydbo) {
						if (!q.aggregation) {
							Map<String, Answer> answer = calculateSPARQLRepresentation(q);

							double fmax = 0;
							double pmax = 0;
							double rmax = 0;
							for (String query : answer.keySet()) {
								Set<RDFNode> systemAnswers = answer.	get(query).answerSet;
								// 11. Compare to set of resources from
								// benchmark
								double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
								double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
								double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
								if (fMeasure >= fmax && fMeasure > 0) {
									log.debug(query.toString());
									log.debug("P=" + precision + " R=" + recall + " F=" + fMeasure);
									// if (fMeasure > fmax) {
									// // used if query with score of // 0.4
									// // is in set and a new one with // 0.6
									// // comes into save only worthy
									// // queries with constant
									// // f-measure
									// }
									fmax = fMeasure;
									pmax = precision;
									rmax = recall;
								}
								this.qw.write(answer.get(query));
							}
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
			}
		}
		log.debug("Average P=" + overallp / counter + " R=" + overallr / counter + " F=" + overallf / counter + " Counter=" + counter);
		this.qw.close();

	}

	// public static void main(String args[]) throws IOException {
	// // #######################
	// // Calculated the F@N
	// // #######################
	// log.info("Configuring controller");
	// Set<Set<Feature>> featureSets = Sets.powerSet(new
	// HashSet<>(Arrays.asList(Feature.values())));
	// RankingEvalPipeline controller = new RankingEvalPipeline();
	//
	// for (Set<Feature> featureSet : featureSets) {
	// if (!featureSet.isEmpty()) {
	// log.info("Training of the ranking function");
	// controller.ranker.setFeatures(featureSet);
	// controller.ranker.train();
	//
	// for (String file : new String[] { "resources/qald-5_train.xml" }) { //
	// test_withanswers
	// controller.dataset = new File(file).getAbsolutePath();
	// log.info("Run controller");
	// controller.run(featureSet);
	// }
	// log.info("Writing results");
	// }
	// }
	//
	// }
	public static void main(String args[]) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		log.info("Configuring controller");
		OverlapRankingEvalPipeline controller = new OverlapRankingEvalPipeline();

		for (String file : new String[] { "resources/qald-5_test_questions.xml" }) { // test_withanswers
			controller.dataset = new File(file).getAbsolutePath();
			log.info("Run controller");
			controller.run();
		}

	}

	public Map<String, Answer> calculateSPARQLRepresentation(Question q) {
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
		Map<String, Answer> answers = queryBuilder.build(q);
		
		OverlapBucketRanker obr = new OverlapBucketRanker();
		answers = obr.rank(answers, 1);
		return answers;
	}

}