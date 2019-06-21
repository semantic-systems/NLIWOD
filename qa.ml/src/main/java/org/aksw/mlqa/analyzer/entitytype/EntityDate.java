package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

public class EntityDate extends Entity implements IAnalyzer {
	// private static Logger log = LoggerFactory.getLogger(EntityDate.class);
	private Attribute attribute = null;
	
	public EntityDate() {
		ArrayList<String> fvWekaPercent = new ArrayList<String>();
		fvWekaPercent.add("Date");
		fvWekaPercent.add("NoDate");
		attribute = new Attribute("Date", fvWekaPercent);
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
