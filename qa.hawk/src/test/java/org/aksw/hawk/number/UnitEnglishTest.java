package org.aksw.hawk.number;

import java.util.HashMap;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.number.UnitEnglish;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitEnglishTest {
	private UnitEnglish unit;
	private static Logger log = LoggerFactory.getLogger(UnitEnglishTest.class);

	@Before
	public void before() {
		unit = new UnitEnglish(new StanfordNLPConnector());
	}

	@Test
	public void hawkParseTest() {
		HashMap<String, String> sentenceToSentence = new HashMap<>();
		sentenceToSentence.put("Who is the president by whom 20 million Americans had gained health insurance?", "Who is the president by whom 20000000 Americans had gained health insurance?");
		// This question will be good to go if NER recognizes formula one
		// sentenceToSentence.put("Where was the first race for the greatest
		// Formula One driver of all times?", "Where was the first race for the
		// greatest Formula One driver of all times?");
		sentenceToSentence.put("Which countries have more than ten volcanoes?", "Which countries have more than 10 volcanoes?");
		sentenceToSentence.put("What are the five boroughs of New York?", "What are the 5 boroughs of New York?");
		sentenceToSentence.put("Which presidents of the United States had more than three children?", "Which presidents of the United States had more than 3 children?");
		sentenceToSentence.put("Which locations have more than two caves?", "Which locations have more than 2 caves?");
		sentenceToSentence.put("Which cities have more than 2 million inhabitants?", "Which cities have more than 2000000 inhabitants?");
		sentenceToSentence.put("Give me all world heritage sites designated within the past five years.", "Give me all world heritage sites designated within the past 5 years.");
		sentenceToSentence.put("Does the new Battlestar Galactica series have more episodes than the old one?", "Does the new Battlestar Galactica series have more episodes than the old one?");
		sentenceToSentence.put("Which city does the first person to climb all 14 eight-thousanders come from?", "Which city does the first person to climb all 14 eight-thousanders come from?");
		sentenceToSentence.put("Give me all films produced by Steven Spielberg with a budget of at least $80 million.",
		        "Give me all films produced by Steven Spielberg with a budget of at least $ 80000000.");
		sentenceToSentence.put("List the seven kings of Rome.", "List the 7 kings of Rome.");

		for (String it : sentenceToSentence.keySet()) {
			String s = unit.convert(it);
			if (!sentenceToSentence.get(it).equals(s)) {
				log.debug("Error on QALD Sentence. Should be \n" + sentenceToSentence.get(it) + "\nBut is:\n" + unit.convert(it));
				Assert.fail();
			}
		}
	}

	// @Test
	// public void parseSomeRandomTest() {
	// HashMap<String, String> sToS = new HashMap<>();
	// sToS.put("50 miles", "80467.2 m");
	// sToS.put("one mile", "1609.344 m");
	// sToS.put("three hundred thousand and sixty four", "300064");
	// sToS.put("€60 thousand and $45", "€ 60000 and $ 45");
	// sToS.put("$80 million", "$ 80000000");
	// for (String it : sToS.keySet()) {
	// String s = unit.convert(it);
	// if (!sToS.get(it).equals(s)) {
	// log.debug("Error on QALD Sentence. Should be \n|" + sToS.get(it) +
	// "|\nBut is:\n|" + unit.convert(it) + "|");
	// }
	// }
	//
	// }

}
