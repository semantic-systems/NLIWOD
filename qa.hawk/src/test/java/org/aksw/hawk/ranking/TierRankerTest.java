package org.aksw.hawk.ranking;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.experiment.SingleQuestionPipeline;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class TierRankerTest {
	// TODO Christian: couldn't test, couldn't continue past SPARQLQueryBuilder
	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);

	@Test
	@Ignore
	public void test() {

		PipelineStanford pipeline = new PipelineStanford();

		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "What is the capital of Spain called?");
		String correctAnswer = "[http://dbpedia.org/resource/Madrid]";
		log.info("Run pipeline on " + q.getLanguageToQuestion().get("en") + ", expecting answer: " + correctAnswer);
		// log.info("Run pipeline on " + q.languageToQuestion.get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		// ##############~~RANKING~~##############
		log.info("Run ranking");
		int maximumPositionToMeasure = 10;

		TierRanker tier = new TierRanker();

		log.info("Tier-based ranking");
		List<Answer> rankedAnswer = tier.rank(answers, q);
		double maxScore = 0.0;
		double correctScore = 0.0;
		for (Answer ans : rankedAnswer) {
			if (ans.score > maxScore) {
				maxScore = ans.score;
			}
			if ((ans.answerSet.toString().equals(correctAnswer)) && (ans.score > correctScore)) {
				correctScore = ans.score;
			}
		}
		// general test: Ranker finds matching answers to fill buckets
		assertTrue(maxScore >= 0.0);
		log.info("Maximum tier ranking: " + maxScore);
		// optional test: Correct answer corresponds to largest bucket
		assertTrue(correctScore == maxScore);
		log.info("Ranking of correct answer: " + correctScore);
		log.info(Joiner.on("\n\t").join(rankedAnswer));
	}

}
