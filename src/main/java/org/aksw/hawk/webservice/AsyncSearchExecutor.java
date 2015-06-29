package org.aksw.hawk.webservice;

import java.util.List;
import java.util.concurrent.Future;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.controller.Pipeline;
import org.aksw.hawk.ranking.BucketRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;

@Service("asyncSearchExecutor")
public class AsyncSearchExecutor {
	private Pipeline pipeline = new Pipeline();
	private Logger log = LoggerFactory.getLogger(AsyncSearchExecutor.class);

	@Async
	public Future<Question> search(Question q) {
		log.info("Run pipeline on " + q.languageToQuestion.get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		// FIXME improve ranking
		// bucket-based ranking
		BucketRanker bucket_ranker = new BucketRanker();
		log.info("Bucket-based ranking");
		List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		log.info(Joiner.on("\n\t").join(rankedAnswer));
		q.finalAnswer = rankedAnswer;

		return new AsyncResult<Question>(q);
	}
}
