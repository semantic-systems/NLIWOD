package org.aksw.hawk.webservice;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Future;

import javax.servlet.http.HttpServletResponse;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.SimpleIdGenerator;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

@RestController
// @EnableAsync
public class WebController {

	// @Autowired
	// @Qualifier("asyncSearchExecutor")
	@Qualifier("SearchExecutor")
	private SearchExecutor searchExecutor = new SearchExecutor();

	private Logger log = LoggerFactory.getLogger(WebController.class);
	private HashMap<UUID, Future<HAWKQuestion>> runningProcesses = Maps.newHashMap();
	private HashMap<UUID, HAWKQuestion> UuidQuestionMap = Maps.newHashMap();
	private SimpleIdGenerator IdGenerator = new SimpleIdGenerator();

	// TODO refactor that class to simple
	// /search?q=What+is+the+capital+of+Germany+%3F
	@RequestMapping("/search")
	public String search(@RequestParam(value = "q") String question, HttpServletResponse response) {
		log.debug("Received question = " + question);
		// CORS
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");
		response.setHeader("Access-Control-Allow-Headers", "x-requested-with");

		return searchExecutor.runPipeline(question);

		// create a question object
		// HAWKQuestion q = new HAWKQuestion();
		// q.getLanguageToQuestion().put("en", question);
		//
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
		// return q.getJSONStatus();
	}

}
