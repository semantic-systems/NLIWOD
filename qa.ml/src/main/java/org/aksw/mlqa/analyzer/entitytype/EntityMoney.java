package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

public class EntityMoney extends Entity implements IAnalyzer {
	// private static Logger log = LoggerFactory.getLogger(EntityMoney.class);
	private Attribute attribute = null;
	
	public EntityMoney() {
		ArrayList<String> fvWekaPercent = new ArrayList<String>();
		fvWekaPercent.add("Money");
		fvWekaPercent.add("NoMoney");
		attribute = new Attribute("Money", fvWekaPercent);
	}

	@Override
	public Object analyze(String q) {
		return recognizeEntity("Money", q);
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
