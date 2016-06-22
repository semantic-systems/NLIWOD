package org.aksw.hawk.ranking;

import java.util.List;

import org.aksw.hawk.controller.EvalObj;
import org.aksw.hawk.controller.PipelineClearNLP;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.experiment.Measures;
import org.aksw.hawk.experiment.SingleQuestionPipeline;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class OptimalRankerTest {
	// TODO Christian: Optimal ranking not implemented correctly yet
	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);

	@Test
	@Ignore
	public void test() {

		PipelineClearNLP pipeline = new PipelineClearNLP();

		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "Which actress starring in the TV series Friends owns the production company Coquette Productions?");

		log.info("Run pipeline on " + q.getLanguageToQuestion().get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		// ##############~~RANKING~~##############
		log.info("Run ranking");
		int maximumPositionToMeasure = 10;
		OptimalRanker optimal_ranker = new OptimalRanker();

		log.info("Optimal ranking not applicable (right now).");
		List<Answer> rankedAnswer = optimal_ranker.rank(answers, q);
		List<EvalObj> eval = Measures.measure(rankedAnswer, q, maximumPositionToMeasure);
		log.info(Joiner.on("\n\t").join(eval));

	}
}
