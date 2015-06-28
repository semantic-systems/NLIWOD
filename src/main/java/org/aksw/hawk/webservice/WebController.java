package org.aksw.hawk.webservice;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.controller.Pipeline;
import org.aksw.hawk.ranking.BucketRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.SimpleIdGenerator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

@RestController
public class WebController {
	Logger log = LoggerFactory.getLogger(WebController.class);
	private Pipeline pipeline = new Pipeline();
	private SimpleIdGenerator IdGenerator = new SimpleIdGenerator();
	private HashMap<UUID, Question> runningProcesses = Maps.newHashMap();

	// /search?q=What+is+the+capital+of+Germany+%3F
	@RequestMapping("/search")
	public UUID search(@RequestParam(value = "q") String question) {
		// create a question object
		Question q = new Question();
		q.languageToQuestion.put("en", question);
		q.UUID = IdGenerator.generateId();

		// put it to queue to fetch while long lasting processing
		runningProcesses.put(q.UUID, q);

		// start the search process
		startSearch(q);

		// return the UUID
		return q.UUID;
	}

	@RequestMapping("/status")
	public String status(@RequestParam(value = "UUID") UUID UUID) {
		if (runningProcesses.containsKey(UUID)) {
			Question q = runningProcesses.get(UUID);
			if (q.finished) {
				runningProcesses.remove(UUID);
			}
			return q.getJSONStatus();
		} else {
			return "{Error: \"No such search id.\"}";
		}
	}

	private void startSearch(Question q) {
		log.info("Run pipeline on " + q.languageToQuestion.get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		// FIXME improve ranking
		// bucket-based ranking
		BucketRanker bucket_ranker = new BucketRanker();
		log.info("Bucket-based ranking");
		List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		log.info(Joiner.on("\n\t").join(rankedAnswer));
		q.finalAnswer = rankedAnswer;
	}
}
