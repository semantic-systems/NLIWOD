package org.aksw.qa.annotation.webservice;

import java.util.List;
import java.util.Map;

import org.aksw.qa.annotation.spotter.Fox;
import org.aksw.qa.commons.datastructure.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import or.aksw.qa.annotation.index.IndexDBO_classes;
import or.aksw.qa.annotation.index.IndexDBO_properties;

@RestController

@SpringBootApplication
public class FrontController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private IndexDBO_classes classes = new IndexDBO_classes();
	private IndexDBO_properties properties = new IndexDBO_properties();
	private Fox fox = new Fox();

	@RequestMapping("/class")
	public List<String> searchClass(@RequestParam(value = "q") final String q) {

		logger.debug("Requesting class search for term :|" + q + "|");
		List<String> annotated = classes.search(q);

		return annotated;
	}

	@RequestMapping("/property")
	public List<String> searchProperty(@RequestParam(value = "q") final String q) {

		logger.debug("Requesting property search for term :|" + q + "|");
		List<String> annotated = properties.search(q);

		return annotated;
	}

	@RequestMapping("/fox")
	public Map<String, List<Entity>> runFox(@RequestParam(value = "q") final String q) {

		logger.debug("Requesting fox search for term :|" + q + "|");
		return fox.getEntities(q);
	}

	public static void main(final String[] args) {
		SpringApplication.run(FrontController.class, args);

	}

}
