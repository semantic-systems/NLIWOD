package org.aksw.hawk.experiment;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

import org.aksw.hawk.controller.AbstractPipeline;
import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.oldHybridRecursiveQueryBuilding.ranking.BucketRanker;
import org.aksw.hawk.querybuilding.oldHybridRecursiveQueryBuilding.ranking.OptimalRanker;
import org.aksw.hawk.querybuilding.oldHybridRecursiveQueryBuilding.ranking.TierRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * Made for testing a single pipeline
 * 
 * @author Lorenz Buehmann
 * @author ricardousbeck
 * 
 */
public class SingleQuestionPipeline {
	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);

	public static void main(String args[]) throws IOException, ParserConfigurationException, ExecutionException, RuntimeException {
		log.info("Configuring controller");
		AbstractPipeline pipeline = new PipelineStanford();

		HAWKQuestion q = new HAWKQuestion();
//		q.getLanguageToQuestion().put("en", "Which anti-apartheid activist was born in Mvezo?");
		q.getLanguageToQuestion().put("en", "Who is the president of the United States?");

		log.info("Run pipeline on " + q.getLanguageToQuestion().get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		// ##############~~RANKING~~##############
		log.info("Run ranking");
		int maximumPositionToMeasure = 10;
		OptimalRanker optimal_ranker = new OptimalRanker();
		// FeatureBasedRanker feature_ranker = new FeatureBasedRanker();
		BucketRanker bucket_ranker = new BucketRanker();
		TierRanker tier = new TierRanker();
		// optimal ranking
		log.info("Optimal ranking not applicable (right now).");
		// List<Set<RDFNode>> rankedAnswer = optimal_ranker.rank(answers, q);
		// List<EvalObj> eval = Measures.measure(rankedAnswer, q,
		// maximumPositionToMeasure);
		// log.info(Joiner.on("\n\t").join(eval));

		// feature-based ranking
		// log.info("Feature-based ranking begins training.");
		// for (Set<Feature> featureSet : Sets.powerSet(new
		// HashSet<>(Arrays.asList(Feature.values())))) {
		// if (!featureSet.isEmpty()) {
		// log.debug("Feature-based ranking: " + featureSet.toString());
		// feature_ranker.setFeatures(featureSet);
		// feature_ranker.train();
		// List<Answer> rankedAnswer = feature_ranker.rank(answers, q);
		// log.info(Joiner.on("\n\t").join(rankedAnswer));
		// }
		// }

		// bucket-based ranking
		log.info("Bucket-based ranking");
		List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		log.info(Joiner.on("\n\t").join(rankedAnswer));

		// tier-based ranking
		log.info("Tier-based ranking");
		rankedAnswer = tier.rank(answers, q);
		log.info(Joiner.on("\n\t").join(rankedAnswer));

	}

}
