package org.aksw.hawk.webservice;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.util.GerbilFinalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RestController
public class GerbilQABenchmarking {
	private Logger log = LoggerFactory.getLogger(GerbilQABenchmarking.class);
	private PipelineStanford pipeline = new PipelineStanford();

	// test via
	// curl -d "query=Who is the president of Europe?&lang=en" -X POST http://localhost:8181/ask-gerbil
	@RequestMapping(value = "/ask-gerbil", method = RequestMethod.POST)
	public String askGerbil(@RequestParam Map<String, String> params, final HttpServletResponse response) throws ExecutionException, RuntimeException, IOException {
		log.debug("Received question = " + params.get("query"));
		log.debug("Language of question = " + params.get("lang"));
		// CORS to allow for communication between https and http
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

		String question = params.get("query");
		String lang = params.get("lang");

		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", question);
		List<Answer> answer = pipeline.getAnswersToQuestion(q);
		q.setFinalAnswer(answer);

		GerbilFinalResponse resp = new GerbilFinalResponse();
		resp.setQuestions(q);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(resp);

		log.info("\n\n JSON object: \n\n" + json);

		return json;
	}

}