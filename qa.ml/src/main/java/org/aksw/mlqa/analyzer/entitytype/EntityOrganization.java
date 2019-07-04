package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

/***
 * Analyzes if there is an entity of type Organization in the question.
 * @author Lukas
 *
 */
public class EntityOrganization extends Entity implements IAnalyzer {
	private Attribute attribute = null;
	
	public EntityOrganization() {
		ArrayList<String> fvWekaOrganization = new ArrayList<String>();
		fvWekaOrganization.add("Organization");
		fvWekaOrganization.add("NoOrganization");
		attribute = new Attribute("Organization", fvWekaOrganization);
	}

	@Override
	public Object analyze(String q) {
		return recognizeEntity("Organization", q);
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}


