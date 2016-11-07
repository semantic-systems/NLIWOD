package org.aksw.hawk.webservice;

import javax.servlet.http.HttpServletResponse;

import org.aksw.hawk.controller.PipelineStanford;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Component
// @EnableAsync
public class WebController {
	private Logger log = LoggerFactory.getLogger(WebController.class);
	private PipelineStanford pipeline = new PipelineStanford();
	private NounCombinationWeb nouns = new NounCombinationWeb(pipeline.getStanfordConnector());

	// @Autowired
	// @Qualifier("asyncSearchExecutor")
	@Qualifier("SearchExecutor")
	private SearchExecutor searchExecutor = new SearchExecutor();

	public WebController() {
		searchExecutor.setPipeline(pipeline);
	}

	@RequestMapping("/search")
	public String search(@RequestParam(value = "q") final String question, final HttpServletResponse response) {
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

	@RequestMapping(method = RequestMethod.GET, value = "/nounphrase")
	public String nounphraseGET(@RequestParam(value = "q") final String question) {
		return nouns.stringToNif(question);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/nounphrase")
	public String nounphrasePOST(@RequestBody final String input) {
		return nouns.nifToAnswerNif(input);
	}

}
