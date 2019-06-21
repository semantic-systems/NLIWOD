package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

public class EntityLocation extends Entity implements IAnalyzer {
	//static Logger log = LoggerFactory.getLogger(EntityLocation.class);
	private Attribute attribute = null;
	
	public EntityLocation() {
		ArrayList<String> fvWekaPercent = new ArrayList<String>();
		fvWekaPercent.add("Location");
		fvWekaPercent.add("NoLocation");
		attribute = new Attribute("Location", fvWekaPercent);
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
