package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;


public class EntityOrganization extends Entity implements IAnalyzer {
	//static Logger log = LoggerFactory.getLogger(EntityOrganization.class);
	private Attribute attribute = null;
	
	public EntityOrganization() {
		ArrayList<String> fvWekaPercent = new ArrayList<String>();
		fvWekaPercent.add("Organization");
		fvWekaPercent.add("NoOrganization");
		attribute = new Attribute("Organization", fvWekaPercent);
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

