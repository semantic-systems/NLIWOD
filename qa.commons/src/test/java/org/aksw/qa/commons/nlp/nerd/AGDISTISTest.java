package org.aksw.qa.commons.nlp.nerd;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AGDISTISTest {
	private Logger log = LoggerFactory.getLogger(AGDISTISTest.class);
	
	@Test
	public void testDisambiguation() throws ParseException, IOException {
		AGDISTIS post = new AGDISTIS();
		String subjectString = "Tom Cruise";
		String objectString = "Katie Holmes";

		String preAnnotatedText = "<entity>" + subjectString + "</entity><entity>" + objectString + "</entity>";

		log.debug("Disambiguation for: " + preAnnotatedText);
		
		HashMap<String, String> realResults = new LinkedHashMap<String,String>();
		realResults.put("Katie Holmes", "http://dbpedia.org/resource/Katie_Holmes");
		realResults.put("Tom Cruise", "http://dbpedia.org/resource/Tom_Cruise");
		
		HashMap<String, String> results = post.runDisambiguation(preAnnotatedText);
		for (String namedEntity : results.keySet()) {
			Assert.assertTrue(results.get(namedEntity).equals(realResults.get(namedEntity)));
			log.debug("named entity: " + namedEntity + " -> " + results.get(namedEntity));
		}
	}
}
