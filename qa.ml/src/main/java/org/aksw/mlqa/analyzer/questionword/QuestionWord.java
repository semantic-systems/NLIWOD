package org.aksw.mlqa.analyzer.questionword;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;

/**
 * Analyzes what type of question word it is, like Who, Where, and What. Groups List, Give, Show as Commands and
 * Is, Are, Did, Does, Was, Do as AuxVerb.
 * 
 * @author ricardousbeck
 *
 */
public class QuestionWord implements IAnalyzer {
	private Logger log = LoggerFactory.getLogger(QuestionWord.class);
	private Attribute attribute = null;
	private String AuxVerb = "Is||Are||Did||Does||Was||Do";
	private String Commands = "Give||Show||List";

	public QuestionWord() {
		
		ArrayList<String> attributeValues = new ArrayList<String>();
		attributeValues.add("Who");
		attributeValues.add("What");
		attributeValues.add("When");
		attributeValues.add("Where");
		attributeValues.add("Which");
		attributeValues.add(Commands);
		attributeValues.add(AuxVerb);
		attributeValues.add("How");
		attributeValues.add("Misc");

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
