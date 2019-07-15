package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

/***
 * Analyzes if there is an entity of type Location in the question.
 * @author Lukas
 *
 */
public class EntityLocation extends Entity implements IAnalyzer {
	private Attribute attribute = null;
	
	public EntityLocation() {
		ArrayList<String> fvWekaLocation = new ArrayList<String>();
		fvWekaLocation.add("Location");
		fvWekaLocation.add("NoLocation");
		attribute = new Attribute("Location", fvWekaLocation);
	}

	@Override
	public Object analyze(String q) {
		return recognizeEntity("Location", q);
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
