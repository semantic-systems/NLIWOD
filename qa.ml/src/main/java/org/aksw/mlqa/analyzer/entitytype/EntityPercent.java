package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

public class EntityPercent extends Entity implements IAnalyzer {
	//static Logger log = LoggerFactory.getLogger(EntityPercent.class);
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

