package org.aksw.hawk.webservice;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.aksw.hawk.controller.AbstractPipeline;
import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.oldHybridRecursiveQueryBuilding.ranking.BucketRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;

@Component
@Service("searchExecutor")
public class SearchExecutor {
	private AbstractPipeline pipeline = new PipelineStanford();
	private Logger log = LoggerFactory.getLogger(SearchExecutor.class);


	public void setPipeline(final AbstractPipeline pipeline) {
		this.pipeline = pipeline;
	}

	public String runPipeline(final String question) throws ExecutionException, RuntimeException {
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", question);
		log.info("Run pipeline on " + q.getLanguageToQuestion().get("en"));
		//log.info("q value" + q);
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		BucketRanker bucket_ranker = new BucketRanker();
		log.info("Bucket-based ranking");
		List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		log.info(Joiner.on("\n\t").join(rankedAnswer));
		q.setFinalAnswer(rankedAnswer);
		return q.getJSONStatus();
	}

}
