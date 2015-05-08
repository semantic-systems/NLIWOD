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

public class VotingBasedRankingEvalPipeline {
	static Logger log = LoggerFactory.getLogger(VotingBasedRankingEvalPipeline.class);
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

	public VotingBasedRankingEvalPipeline() {

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

	void run(Set<Feature> featureSet) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		qw = new QALDWriter(dataset);

//		for (int count : Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50)) {
		for (int count : Lists.newArrayList(1)) {

		List<Question> questions = datasetLoader.load(dataset);
			double overallf = 0;
			double overallp = 0;
			double overallr = 0;
			double counter = 0;
			for (Question q : questions) {
				if (q.hybrid) {
					if (q.answerType.equals("resource")) {
						if (q.onlydbo) {
							if (!q.aggregation) {
								String question = q.languageToQuestion.get("en");
								Map<String, Answer> answer = calculateSPARQLRepresentation(q, featureSet);

								double fmax = 0;
								double pmax = 0;
								double rmax = 0;
								int i = 0;
								for (String query : answer.keySet()) {
									if (i < count) {
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
												// used if query with score of
												// 0.4
												// is in set and a new one with
												// 0.6
												// comes into save only worthy
												// queries with constant
												// f-measure
											}
											fmax = fMeasure;
											pmax = precision;
											rmax = recall;
										}
										i++;
										this.qw.write(answer.get(query));
									} else {
										break;
									}
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
					// break;
				}
			}
			this.qw.close();
			log.debug("Features: " + featureSet);
			log.debug("Average P=" + overallp / counter + " R=" + overallr / counter + " F=" + overallf / counter + " Counter=" + counter);
			log.info("F@n\t" + count + "\tF=" + +overallf / counter + "\t" + ranker.getFeatures() + "\t" + dataset);
		}
	}

	public static void main(String args[]) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		// #######################
		// Calculated the F@N
		// #######################
		log.info("Configuring controller");
		// Set<Set<Feature>> featureSets = Sets.powerSet(new
		// HashSet<>(Arrays.asList(Feature.values())));
		VotingBasedRankingEvalPipeline controller = new VotingBasedRankingEvalPipeline();

		// for (Set<Feature> featureSet : featureSets) {
		// if (!featureSet.isEmpty()) {
		Set<Feature> featureSet = new HashSet<>(Arrays.asList(Feature.values()));
		log.info("Training of the ranking function");
		controller.ranker.setFeatures(featureSet);
		controller.ranker.train();

		for (String file : new String[] { "resources/qald-5_train.xml", "resources/qald-5_test_questions.xml" }) { // test_withanswers
			controller.dataset = new File(file).getAbsolutePath();
			log.info("Run controller");
			controller.run(featureSet);
		}
		log.info("Writing results");
		// }
		// }

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

		Map<String, Answer> answer = queryBuilder.buildWithRanking(q, ranker);
		return answer;

	}

}