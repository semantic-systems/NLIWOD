package org.aksw.qa.annotation.webservice;

import org.aksw.qa.annotation.index.IndexDBO_classes;
import org.aksw.qa.annotation.util.NifEverything;
import org.aksw.qa.annotation.util.NifEverything.NifProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/class")
class ClassDBO_ClassRequest {
	/**
	 *
	 */
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private IndexDBO_classes classes;
	private NifEverything nif = NifEverything.getInstance();

	public ClassDBO_ClassRequest() {
		classes = new IndexDBO_classes();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getClass(@RequestParam(value = "q") final String q) {
		logger.debug("Requesting CLASS search for term (VIA GET) :|" + q + "|");
		return nif.createNIFResultFromIndexDBO(q, classes, NifProperty.TACLASSREF);

	}

	@RequestMapping(method = RequestMethod.POST)
	public String postClass(@RequestBody final String input) {
		logger.debug("Requesting CLASS search for term (VIA POST) ");
		logger.trace("|" + input + "|");
		return nif.appendNIFResultFromIndexDBO(input, classes, NifProperty.TACLASSREF);
	}
}