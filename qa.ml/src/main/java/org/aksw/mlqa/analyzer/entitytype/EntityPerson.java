package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

public class EntityPerson extends Entity implements IAnalyzer {
	// private static Logger log = LoggerFactory.getLogger(EntityPerson.class);
	private Attribute attribute = null;
	
	public EntityPerson() {
		ArrayList<String> fvWekaPercent = new ArrayList<String>();
		fvWekaPercent.add("Person");
		fvWekaPercent.add("NoPerson");
		attribute = new Attribute("Person", fvWekaPercent);
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
