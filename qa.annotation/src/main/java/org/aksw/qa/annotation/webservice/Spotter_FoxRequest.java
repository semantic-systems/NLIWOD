package org.aksw.qa.annotation.webservice;

import org.aksw.qa.annotation.spotter.Fox;
import org.aksw.qa.annotation.util.NifEverything;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fox")
public class Spotter_FoxRequest {
	/**
	 *
	 */
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private Fox fox;
	private NifEverything nif = NifEverything.getInstance();

	public Spotter_FoxRequest() {
		fox = new Fox();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getFox(@RequestParam(value = "q") final String q) {
		logger.debug("Requesting NER/FOX for term (VIA GET) :|" + q + "|");
		return nif.createNIFResultFromSpotters(q, fox);

	}

	@RequestMapping(method = RequestMethod.POST)
	public String postFox(@RequestBody final String input) {
		logger.debug("Requesting NER/FOX search for term (VIA POST)");
		logger.trace("|" + input + "|");
		return nif.appendNIFResultFromSpotters(input, fox);
	}
}
