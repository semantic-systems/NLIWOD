package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

/***
 * Analyzes if there is an entity of type Person in the question.
 * @author Lukas
 *
 */
public class EntityPerson extends Entity implements IAnalyzer {
	private Attribute attribute = null;
	
	public EntityPerson() {
		ArrayList<String> fvWekaPerson = new ArrayList<String>();
		fvWekaPerson.add("Person");
		fvWekaPerson.add("NoPerson");
		attribute = new Attribute("Person", fvWekaPerson);
	}

	@Override
	public Object analyze(String q) {
		return recognizeEntity("Person", q);
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}

