package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

/***
 * Analyzes if there is an entity of type Percent in the question.
 * @author Lukas
 *
 */
public class EntityPercent extends Entity implements IAnalyzer {
	private Attribute attribute = null;
		
	public EntityPercent() {
		ArrayList<String> fvWekaPercent = new ArrayList<String>();
		fvWekaPercent.add("Percent");
		fvWekaPercent.add("NoPercent");
		attribute = new Attribute("Percent", fvWekaPercent);
	}

	@Override
	public Object analyze(String q) {
		return recognizeEntity("Percent", q);
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}


