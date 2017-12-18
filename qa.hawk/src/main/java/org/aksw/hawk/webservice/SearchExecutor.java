package org.aksw.hawk.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.aksw.hawk.controller.AbstractPipeline;
import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
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
	public List<Answer> rankedAnswer = new ArrayList<>();


	public void setPipeline(final AbstractPipeline pipeline) {
		this.pipeline = pipeline;
	}
	
	int uniqueID = 0;
	
	public synchronized long getUniqueId()
	{
	    return uniqueID++;
	}

	public HAWKQuestion runPipeline(final String question) throws ExecutionException, RuntimeException {
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", question);
		q.setId(String.valueOf(this.getUniqueId()));
		log.info("Run pipeline on " + q.getLanguageToQuestion().get("en"));
		//log.info("q value" + q);
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		log.info(Joiner.on("\n\t").join(answers));
		q.setFinalAnswer(answers);
		return q.getJSONStatus();
	}

}
