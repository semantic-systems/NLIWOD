package org.aksw.hawk.webservice;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.oldHybridRecursiveQueryBuilding.ranking.BucketRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;

@Service("asyncSearchExecutor")
public class AsyncSearchExecutor {
	private PipelineStanford pipeline = new PipelineStanford();
	private Logger log = LoggerFactory.getLogger(AsyncSearchExecutor.class);

	@Async
	public Future<HAWKQuestion> search(HAWKQuestion q) throws ExecutionException, RuntimeException {
		log.info("Run pipeline on " + q.getLanguageToQuestion().get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		// FIXME improve ranking, put other ranking method here
		// bucket-based ranking
		BucketRanker bucket_ranker = new BucketRanker();
		log.info("Bucket-based ranking");
		List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		log.info(Joiner.on("\n\t").join(rankedAnswer));
		q.setFinalAnswer(rankedAnswer);

		return new AsyncResult<HAWKQuestion>(q);
	}
}