package org.aksw.qa.commons.knowledgeCard;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.jsonldjava.utils.JsonUtils;

@RestController
@EnableAutoConfiguration
public class KnowledgeCardController {
	private Logger log = LoggerFactory.getLogger(KnowledgeCardController.class);
	
	@Autowired
	private KnowledgeCardCreator knowledgeCardCreator;

	@RequestMapping(value = "/knowledgeCard", method = RequestMethod.POST)
	public String askGenesis(@RequestParam String url, final HttpServletResponse response)
			throws ExecutionException, RuntimeException, IOException, ParseException {
		HashSet<Field> answer = knowledgeCardCreator.process(url);
		String prettyString = JsonUtils.toPrettyString(answer);
		log.info("Got: " + prettyString);
		return prettyString;
	}

}
