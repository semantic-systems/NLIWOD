package org.aksw.hawk.webservice;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.aksw.autosparql.commons.qald.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.util.SimpleIdGenerator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;

@RestController
@EnableAsync
public class WebController {

	@Autowired
	@Qualifier("asyncSearchExecutor")
	private AsyncSearchExecutor asyncSearchExecutor;

	private Logger log = LoggerFactory.getLogger(WebController.class);
	private HashMap<UUID, Future<Question>> runningProcesses = Maps.newHashMap();
	private HashMap<UUID, Question> UuidQuestionMap = Maps.newHashMap();
	private SimpleIdGenerator IdGenerator = new SimpleIdGenerator();

	// /search?q=What+is+the+capital+of+Germany+%3F
	@RequestMapping("/search")
	public UUID search(@RequestParam(value = "q") String question) {
		// create a question object
		Question q = new Question();
		q.languageToQuestion.put("en", question);
		q.UUID = IdGenerator.generateId();

		// start the search process
		Future<Question> report = asyncSearchExecutor.search(q);

		// put it to queue to fetch while long lasting processing
		runningProcesses.put(q.UUID, report);
		UuidQuestionMap.put(q.UUID, q);

		// return the UUID
		return q.UUID;
	}

	@RequestMapping("/status")
	public String status(@RequestParam(value = "UUID") UUID UUID) {
		if (runningProcesses.containsKey(UUID)) {
			Future<Question> q = runningProcesses.get(UUID);
			if (q.isDone()) {
				runningProcesses.remove(UUID);
				try {
					return q.get().getJSONStatus();
				} catch (InterruptedException | ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				return UuidQuestionMap.get(UUID).getJSONStatus();
			}
		} else if (UuidQuestionMap.containsKey(UUID)) {
			// finished working on this query
			return UuidQuestionMap.get(UUID).getJSONStatus();
		}
		return "{Error: \"No such search id.\"}";
	}
}
