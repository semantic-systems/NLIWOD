package hawk;

import org.aksw.hawk.experiment.SingleQuestionPipeline;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BucketRankerTest {

	@Before
	public void setUp() throws Exception {
	}

	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);

	@Test
	public void test() {
		// TODO fix
		// Pipeline pipeline = new Pipeline();
		//
		// HAWKQuestion q = new HAWKQuestion();
		// q.getLanguageToQuestion().put("en",
		// "Who was the president who authorized atomic weapons against Japan during World War II?");
		// String correctAnswer =
		// "[http://dbpedia.org/resource/Harry_S._Truman]";
		// log.info("Run pipeline on " + q.getLanguageToQuestion().get("en") +
		// ", expecting Answer:" + correctAnswer);
		// List<Answer> answers = pipeline.getAnswersToQuestion(q);
		//
		// // ##############~~RANKING~~##############
		// log.info("Run ranking");
		// int maximumPositionToMeasure = 10;
		// //
		// BucketRanker bucket_ranker = new BucketRanker();
		//
		// // bucket-based ranking
		// log.info("Bucket-based ranking");
		// List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		// log.info(Joiner.on("\n\t").join(rankedAnswer));
		// double maxScore = 1.0;
		// double correctScore = 1.0;
		// for (Answer ans : rankedAnswer) {
		// if (ans.score > maxScore) {
		// maxScore = ans.score;
		// }
		// if (ans.answerSet.toString().equals(correctAnswer)) {
		// correctScore = ans.score;
		// }
		// }
		// // general test: Ranker finds matching answers to fill buckets
		// System.out.println(maxScore);
		// assertTrue(maxScore > 1.0);
		// log.info("Maximum bucket ranking: " + maxScore);
		// // optional test: Correct answer corresponds to largest bucket
		// // assertTrue(correctScore==maxScore);

	}

}
