package org.aksw.mlqa.analyzer.questionword;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.FastVector;

//TODO write unit test for this analyzer
/**
 * Analyzes what type of question word is it, like Who Where What Give Me
 * 
 * @author ricardousbeck
 *
 */
public class QuestionWord implements IAnalyzer {
	Logger log = LoggerFactory.getLogger(QuestionWord.class);
	private Attribute attribute = null;
	String AuxVerb = "Is||Are||Did";
	String Commands = "Give||Show";

	public QuestionWord() {
		FastVector attributeValues = new FastVector();
		attributeValues.addElement("Who");
		attributeValues.addElement("What");
		attributeValues.addElement("When");
		attributeValues.addElement("Where");
		attributeValues.addElement("Which");
		attributeValues.addElement(Commands);
		attributeValues.addElement(AuxVerb);
		attributeValues.addElement("How");
		attributeValues.addElement("Misc");

		attribute = new Attribute("QuestionWord", attributeValues);
	}

	@Override
	public Object analyze(String q) {
		log.debug("String question: " + q);
		String[] split = q.split("\\s+");
		int indexOfValue = attribute.indexOfValue(split[0]);
		if (indexOfValue < 0) {
			// catch the auxiliary verbs and commands as one nominal dimension
			if (split[0].matches(AuxVerb)) {
				indexOfValue = attribute.indexOfValue(AuxVerb);
			} else if (split[0].matches(Commands)) {
				indexOfValue = attribute.indexOfValue(Commands);
			} else {
				indexOfValue = attribute.indexOfValue("Misc");
			}
		}
		return attribute.value(indexOfValue);
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
