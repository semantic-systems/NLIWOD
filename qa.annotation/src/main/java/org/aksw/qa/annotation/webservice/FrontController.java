package org.aksw.qa.annotation.webservice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.TypedSpanImpl;
import org.aksw.qa.annotation.spotter.Fox;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import or.aksw.qa.annotation.index.IndexDBO;
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
	public String searchClass(@RequestParam(value = "q") final String q) {
		logger.debug("Requesting class search for term :|" + q + "|");
		return createNIFResultFromIndex(q, classes);

	}

	@RequestMapping("/property")
	public String searchProperty(@RequestParam(value = "q") final String q) {
		logger.debug("Requesting class search for term :|" + q + "|");
		return createNIFResultFromIndex(q, properties);
	}

	@RequestMapping("/fox")
	public Map<String, List<Entity>> runFox(@RequestParam(value = "q") final String q) {

		logger.debug("Requesting fox search for term :|" + q + "|");
		return fox.getEntities(q);
	}

	public static void main(final String[] args) {
		SpringApplication.run(FrontController.class, args);

	}

	private List<ImmutablePair<String, Integer>> extractSplitQuestion(String q) {
		List<ImmutablePair<String, Integer>> ret = new ArrayList<>();
		// q =
		// q.replaceAll("(.)*(nif:isString)(\\s)*(.)+(\")(\\s)*(\\p{Punct})",
		// "$4");
		q = q.replaceAll("(\\p{Punct})", " $1");
		int wordIndex = 0;
		for (String s : q.split(" ")) {
			ret.add(new ImmutablePair<>(s, wordIndex));
			wordIndex += s.length() + 1;
		}
		return ret;

	}

	private String createNIFResultFromIndex(final String q, final IndexDBO indexDBO) {
		Document doc = new DocumentImpl(q);
		for (ImmutablePair<String, Integer> it : extractSplitQuestion(q)) {
			List<String> foundClasses = indexDBO.search(it.getLeft());
			if (CollectionUtils.isEmpty(foundClasses)) {
				continue;
			}
			Marking marking = new TypedSpanImpl(it.getRight(), it.getLeft().length(), new HashSet<>(foundClasses));
			doc.addMarking(marking);
		}

		List<Document> documents = new ArrayList<>();
		documents.add(doc);
		NIFWriter writer = new TurtleNIFWriter();
		String nifString = writer.writeNIF(documents);
		return nifString;
	}

}
