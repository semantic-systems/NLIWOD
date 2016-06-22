package org.aksw.hawk.webservice;

import java.util.List;

import org.aksw.hawk.controller.AbstractPipeline;
import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.ranking.BucketRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;

@Service("searchExecutor")
public class SearchExecutor {
	private AbstractPipeline pipeline = new PipelineStanford();
	private Logger log = LoggerFactory.getLogger(SearchExecutor.class);

	// public Future<HAWKQuestion> search(HAWKQuestion q) {
	// log.info("Run pipeline on " + q.getLanguageToQuestion().get("en"));
	// List<Answer> answers = pipeline.getAnswersToQuestion(q);
	//
	// // FIXME improve ranking, put other ranking method here
	// // bucket-based ranking
	// BucketRanker bucket_ranker = new BucketRanker();
	// log.info("Bucket-based ranking");
	// List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
	// log.info(Joiner.on("\n\t").join(rankedAnswer));
	// q.setFinalAnswer(rankedAnswer);
	//
	// return new AsyncResult<HAWKQuestion>(q);
	// }

	public String runPipeline(String question) {
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", question);
		log.info("Run pipeline on " + q.getLanguageToQuestion().get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		BucketRanker bucket_ranker = new BucketRanker();
		log.info("Bucket-based ranking");
		List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		log.info(Joiner.on("\n\t").join(rankedAnswer));
		q.setFinalAnswer(rankedAnswer);
		return q.getJSONStatus();
	}
}
