package org.aksw.hawk.webservice;

import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimpleSearchController {
	private Logger log = LoggerFactory.getLogger(SimpleSearchController.class);
	private PipelineStanford pipeline = new PipelineStanford();

	// test via
	// curl -d "query=Who is the president of Europe?" -X POST http://localhost:8181/simple-search
	@RequestMapping("/simple-search")
	public String simplesearch(@RequestParam(value = "query") final String question, final HttpServletResponse response) throws ExecutionException, RuntimeException {
		log.debug("Received question = " + question);
		// CORS
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", question);
		List<Answer> answersToQuestion = pipeline.getAnswersToQuestion(q);
		q.setFinalAnswer(answersToQuestion);
		return q.getJSONStatus();
	}

}
