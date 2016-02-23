package org.aksw.hawk.ranking;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.hawk.controller.Pipeline;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.Question;
import org.aksw.hawk.experiment.SingleQuestionPipeline;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.hawk.ranking.FeatureBasedRanker.Feature;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class TierRankerTest {
	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);
	@Test
	public void test() {

		Pipeline pipeline = new Pipeline();

		Question q = new Question();
		q.languageToQuestion.put("en", "In which city in Spain is the royal palace located?");
		String correctAnswer="[http://dbpedia.org/resource/Madrid]";
		log.info("Run pipeline on " + q.languageToQuestion.get("en")+ ", expecting Answer:" + correctAnswer);
		log.info("Run pipeline on " + q.languageToQuestion.get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		// ##############~~RANKING~~##############
		log.info("Run ranking");
		int maximumPositionToMeasure = 10;

		TierRanker tier = new TierRanker();

		log.info("Tier-based ranking");
		List<Answer> rankedAnswer = tier.rank(answers, q);
		double maxScore=0.0;
		double correctScore=0.0;
		for (Answer ans: rankedAnswer)
		{			
			if (ans.score>maxScore)
			{
				maxScore=ans.score;
			}
			if ((ans.answerSet.toString().equals(correctAnswer))&& (ans.score>correctScore))
			{
				correctScore=ans.score;
			}
		}
		//general test: Ranker finds matching answers to fill buckets
		assertTrue(maxScore>0.0);
		log.info("Maximum tier ranking: "+maxScore);
		//optional test: Correct answer corresponds to largest bucket
		assertTrue(correctScore==maxScore);
		log.info("Ranking of correct answer: "+ correctScore);
		log.info(Joiner.on("\n\t").join(rankedAnswer));
	}

}


