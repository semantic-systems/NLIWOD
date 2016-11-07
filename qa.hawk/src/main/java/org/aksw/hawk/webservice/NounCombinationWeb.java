package org.aksw.hawk.webservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nouncombination.NounCombinationChain;
import org.aksw.hawk.nouncombination.NounCombiners;
import org.aksw.qa.commons.datastructure.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NounCombinationWeb {

	NounCombinationChain chainHawkRules;
	StanfordNLPConnector stanford;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public NounCombinationWeb(final StanfordNLPConnector stanford) {
		this.stanford = stanford;
		chainHawkRules = new NounCombinationChain(NounCombiners.HawkRules);
	}

	public String nifToAnswerNif(final String nifString) {
		NIFParser parser = new TurtleNIFParser();
		List<Document> docs = null;
		try {
			docs = parser.parseNIF(nifString);
			if (docs == null) {
				throw new IOException("nif==null");
			}

		} catch (Exception e) {
			log.debug("Couldnt parse input Nif", e);
			return "Invalid input";
		}
		String questionString = docs.get(0).getText();
		return stringToNif(questionString);
	}

	public String stringToNif(final String questionString) {
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", questionString);
		stanford.parseTree(q, null);

		chainHawkRules.runChain(q);

		List<Entity> nounPhrases = q.getLanguageToNounPhrases().get("en");
		if (nounPhrases == null) {
			return null;
		}
		Document newDoc = new DocumentImpl(questionString);
		for (Entity entity : nounPhrases) {
			int offset = 0;
			ArrayList<String> sentence = new ArrayList<>(Arrays.asList(questionString.split(" ")));
			for (int i = 0; i < (entity.getOffset() - 1); i++) {
				offset += sentence.get(i).length() + 1;
			}
			Marking span = new NamedEntity(offset, entity.getLabel().length(), entity.getUris().get(0).getURI());

			newDoc.addMarking(span);
		}
		List<Document> newDocs = new ArrayList<>(Arrays.asList(newDoc));
		NIFWriter writer = new TurtleNIFWriter();

		return writer.writeNIF(newDocs);
	}

	// public Document gettestdoc() {
	// String q = "Why is Barack Obama always better dressed than Michelle
	// Obama?";
	// String namedEntity1 = "Barack Obama";
	// String namedEntity2 = "Michelle Obama";
	// Document doc = new DocumentImpl(q);
	// NamedEntity obama = new NamedEntity(q.indexOf(namedEntity1),
	// namedEntity1.length(), "someUri", true);
	// NamedEntity michelle = new NamedEntity(q.indexOf(namedEntity2),
	// namedEntity2.length(), "someUri2", true);
	// doc.addMarking(obama);
	// doc.addMarking(michelle);
	// return doc;
	// }
	//
	// public static void main(final String[] args) {
	// NIFWriter writer = new TurtleNIFWriter();
	// String nif = writer.writeNIF(new
	// ArrayList<>(Arrays.asList(NounCombinationWeb.getInstance().gettestdoc())));
	// System.out.println(NounCombinationWeb.getInstance().getAnnotationString(nif));
	//
	// }

}
