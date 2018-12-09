package org.aksw.qa.annotation.webservice;

import org.aksw.qa.annotation.sparql.PatternSparqlGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sparql")
public class SparqlPatternRequest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private PatternSparqlGenerator sparql = PatternSparqlGenerator.getInstance();

	@RequestMapping(method = RequestMethod.GET)
	public String getClass(@RequestParam(value = "q") final String q) {
		logger.debug("Requesting Sparql Pattern VIA GET - forbidden");
		return "GET forbidden";

	}

	@RequestMapping(method = RequestMethod.POST)
	public String postClass(@RequestBody final String input) {
		return sparql.nifStrigToQuery(input);
	}

}
