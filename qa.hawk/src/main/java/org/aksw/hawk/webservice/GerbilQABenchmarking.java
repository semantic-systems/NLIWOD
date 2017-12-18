package org.aksw.hawk.webservice;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.util.GerbilFinalResponse;
import org.aksw.hawk.util.GerbilResponseBuilder;
import org.aksw.qa.commons.load.json.*;
import org.apache.commons.math3.geometry.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.SimpleIdGenerator;
import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.Maps;

@RestController
@Component
@SpringBootApplication
//@EnableAsync
public class GerbilQABenchmarking{
	private Logger log = LoggerFactory.getLogger(GerbilQABenchmarking.class);
	private PipelineStanford pipeline = new PipelineStanford();
	private GerbilFinalResponse resp = new GerbilFinalResponse();
	
	
	@Qualifier("SearchExecutor")
	private SearchExecutor searchExecutor = new SearchExecutor();
	
	@RequestMapping(value = "/ask-gerbil", method = RequestMethod.POST)
	public String askGerbil(@RequestParam Map<String,String> params, final HttpServletResponse response) throws ExecutionException, RuntimeException, IOException {
		log.debug("Received question = " + params.get("query"));
		log.debug("Language of question = " + params.get("lang"));
		// CORS
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
		
		String question = params.get("query");
		String lang = params.get("lang");
		
		
		HAWKQuestion answer = searchExecutor.runPipeline(question);
		
		resp.setResponse(answer);
		log.info("resp value: " + resp);
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = ow.writeValueAsString(resp);
		
		log.info("\n\n JSON object: \n\n" + json);
		
		//ExtendedQALDJSONLoader.writeJson(resp, new File("./from_qald_to_extended_to_question_to_qald.json"), true);
		log.info("Final Answer in object: " +answer.getFinalAnswer() + " Answertype: " + answer.getAnswerType() + " Sparql query: " + answer.getSparqlQuery("en"));
		return answer.toString();
	}
	
	public static void main(final String[] args) {
		SpringApplication.run(GerbilQABenchmarking.class, args);
	}
}