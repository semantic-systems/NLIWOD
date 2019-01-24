package org.aksw.qa.annotation.webservice;

import org.aksw.qa.annotation.index.IndexDBO_properties;
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
@RequestMapping("/property")
public class ClassDBO_PropertyRequest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private IndexDBO_properties properties;
	private NifEverything nif = NifEverything.getInstance();

	public ClassDBO_PropertyRequest() {
		properties = new IndexDBO_properties();
	}

	@RequestMapping(method = RequestMethod.GET)
	public String getProperty(@RequestParam(value = "q") final String q) {
		logger.debug("Requesting PROPERTY search for term (VIA GET) :|" + q + "|");
		return nif.createNIFResultFromIndexDBO(q, properties, NifProperty.TAIDENTREF);
	}

	@RequestMapping(method = RequestMethod.POST)
	public String postProperty(@RequestBody final String input) {
		logger.debug("Requesting PROPERTY search for term (VIA POST) ");
		logger.trace("|" + input + "|");
		return nif.appendNIFResultFromIndexDBO(input, properties, NifProperty.TAIDENTREF);
	}
}
