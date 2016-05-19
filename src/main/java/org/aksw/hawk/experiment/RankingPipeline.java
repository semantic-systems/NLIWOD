package org.aksw.hawk.experiment;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.aksw.hawk.controller.AbstractPipeline;
import org.aksw.hawk.controller.EvalObj;
import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.ranking.BucketRanker;
import org.aksw.hawk.ranking.FeatureBasedRanker;
import org.aksw.hawk.ranking.OptimalRanker;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

/**
 * F@N + all ranking experiments for ESWC 2015 publication Possibly extendible
 * for testing NER things
 * 
 * @author Lorenz Buehmann
 * @author ricardousbeck
 * 
 */
public class RankingPipeline {
	static Logger log = LoggerFactory.getLogger(RankingPipeline.class);

	public static void main(String args[]) throws IOException, ParserConfigurationException {
		log.info("Configuring controller");
		AbstractPipeline pipeline = new PipelineStanford();

		log.info("Loading dataset");
		List<HAWKQuestion> questions = HAWKQuestionFactory.createInstances(QALD_Loader.load(Dataset.QALD6_Train_Multilingual));

		for (HAWKQuestion q : questions) {
			if ((q.getHybrid() & q.getAnswerType().equals("resource") & q.getOnlydbo() & !q.getAggregation()) || q.getLoadedAsASKQuery()) {

				log.info("Run pipeline on " + q.getLanguageToQuestion().get("en"));
				List<Answer> answers = pipeline.getAnswersToQuestion(q);

				// ##############~~RANKING~~##############
				log.info("Run ranking");
				int maximumPositionToMeasure = 10;
				OptimalRanker optimal_ranker = new OptimalRanker();
				FeatureBasedRanker feature_ranker = new FeatureBasedRanker();
				BucketRanker bucket_ranker = new BucketRanker();

				// optimal ranking
				// log.info("Optimal ranking");
				// List<Answer> rankedAnswer = optimal_ranker.rank(answers, q);
				// List<EvalObj> eval = Measures.measure(rankedAnswer, q,
				// maximumPositionToMeasure);
				// log.debug(Joiner.on("\n\t").join(eval));

				// correctQueries.add(answer.get(query).query);
				// finalAnswer = answer.get(query);
				// this.ranker.learn(q, correctQueries);

				// feature-based ranking
				// log.info("Feature-based ranking begins training.");
				// for (Set<Feature> featureSet : Sets.powerSet(new
				// HashSet<>(Arrays.asList(Feature.values())))) {
				// if (!featureSet.isEmpty()) {
				// log.debug("Feature-based ranking: " + featureSet.toString());
				// feature_ranker.setFeatures(featureSet);
				// feature_ranker.train();
				// rankedAnswer = feature_ranker.rank(answers, q);
				// eval = Measures.measure(rankedAnswer, q,
				// maximumPositionToMeasure);
				// log.debug(Joiner.on("\n\t").join(eval));
				// }
				// }

				// bucket-based ranking
				log.info("Bucket-based ranking");
				List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
				List<EvalObj> eval = Measures.measure(rankedAnswer, q, maximumPositionToMeasure);
				log.info(Joiner.on("\n\t").join(eval));

				// this.qw.write(finalAnswer);
				// evals.add(new EvalObj(q.id, question, fmax, pmax, rmax,
				// "Assuming Optimal Ranking Function, Spotter: " +
				// nerdModule.toString()));
			}
		}
		// this.qw.close();
		// log.debug("Average P=" + overallp / counter + " R=" + overallr /
		// counter + " F=" + overallf / counter + " Counter=" + counter);

	}

}
