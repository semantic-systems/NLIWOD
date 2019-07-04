package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

/***
 * Analyzes if there is an entity of type Date in the question.
 * @author Lukas
 *
 */
public class EntityDate extends Entity implements IAnalyzer {
	private Attribute attribute = null;
	
	public EntityDate() {
		ArrayList<String> fvWekaDate = new ArrayList<String>();
		fvWekaDate.add("Date");
		fvWekaDate.add("NoDate");
		attribute = new Attribute("Date", fvWekaDate);
	}

	@Override
	public Object analyze(String q) {
		return recognizeEntity("Date", q);
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}

