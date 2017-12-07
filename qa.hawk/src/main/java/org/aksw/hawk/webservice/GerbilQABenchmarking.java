package org.aksw.hawk.webservice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.HAWKQuestion;
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

import com.google.common.collect.Maps;

@RestController
@Component
@SpringBootApplication
@EnableAsync
public class GerbilQABenchmarking{
	private Logger log = LoggerFactory.getLogger(GerbilQABenchmarking.class);
	private PipelineStanford pipeline = new PipelineStanford();
	private SimpleIdGenerator IdGenerator = new SimpleIdGenerator(); 
	
	@Qualifier("SearchExecutor")
	private SearchExecutor searchExecutor = new SearchExecutor();
	
	@RequestMapping(value = "/ask-gerbil", method = RequestMethod.POST)
	public String askGerbil(@RequestParam Map<String,String> params, final HttpServletResponse response) throws ExecutionException, RuntimeException {
		log.debug("Received question = " + params.get("query"));
		log.debug("Language of question = " + params.get("lang"));
		// CORS
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");
		
		String question = params.get("query");
		String lang = params.get("lang");
		
		return searchExecutor.runPipeline(question);
	}
	
	public static void main(final String[] args) {
		SpringApplication.run(GerbilQABenchmarking.class, args);
	}
}