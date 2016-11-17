package org.aksw.hawk.webservice;

import java.util.HashMap;
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
public class WebController {
	private Logger log = LoggerFactory.getLogger(WebController.class);
	private PipelineStanford pipeline = new PipelineStanford();
	private NounCombinationWeb nouns = new NounCombinationWeb(pipeline.getStanfordConnector());
	private SimpleIdGenerator IdGenerator = new SimpleIdGenerator();
	private HashMap<UUID, Future<HAWKQuestion>> runningProcesses = Maps.newHashMap();
	private HashMap<UUID, HAWKQuestion> UuidQuestionMap = Maps.newHashMap();
	
	@Autowired
	@Qualifier("asyncSearchExecutor")
	private AsyncSearchExecutor asyncSearchExecutor;
	
	@Qualifier("SearchExecutor")
	private SearchExecutor searchExecutor = new SearchExecutor();

	public WebController() {
		searchExecutor.setPipeline(pipeline);
	}

	@RequestMapping("/simple-search")
	public String simplesearch(@RequestParam(value = "query") final String question, final HttpServletResponse response) {
		log.debug("Received question = " + question);
		// CORS
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

		return searchExecutor.runPipeline(question);

	}

	@RequestMapping("/search")
	public UUID search(@RequestParam(value = "q") String question, HttpServletResponse response) {
		// CORS
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

		// create a question object
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", question);
		q.setUUID(IdGenerator.generateId());

		// start the search process
		Future<HAWKQuestion> report = asyncSearchExecutor.search(q);

		// put it to queue to fetch while long lasting processing
		runningProcesses.put(q.getUUID(), report);
		UuidQuestionMap.put(q.getUUID(), q);

		// return the UUID
		return q.getUUID();
	}

	@RequestMapping("/status")
	public String status(@RequestParam(value = "UUID") UUID UUID, HttpServletResponse response) {
		// CORS
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

		if (runningProcesses.containsKey(UUID)) {
			Future<HAWKQuestion> q = runningProcesses.get(UUID);
			if (q.isDone()) {
				runningProcesses.remove(UUID);
				try {
					return q.get().getJSONStatus();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			} else {
				return UuidQuestionMap.get(UUID).getJSONStatus();
			}
		} else if (UuidQuestionMap.containsKey(UUID)) {
			// finished working on this query
			return UuidQuestionMap.get(UUID).getJSONStatus();
		}
		throw new SearchIdException(UUID);
	}

	
	@RequestMapping(method = RequestMethod.GET, value = "/nounphrase")
	public String nounphraseGET(@RequestParam(value = "q") final String question) {
		return nouns.stringToNif(question);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/nounphrase")
	public String nounphrasePOST(@RequestBody final String input) {
		return nouns.nifToAnswerNif(input);
	}

	public static void main(final String[] args) {
		SpringApplication.run(WebController.class, args);
	}
}
