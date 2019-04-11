package org.aksw.mlqa.analyzer.superlative;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.qa.annotation.comparison.ComparisonUtils;

import weka.core.Attribute;

public class Superlative implements IAnalyzer {
	// private static Logger log = LoggerFactory.getLogger(Superlative.class);
	private Attribute attribute = null;
	
	public Superlative() {	
		ArrayList<String> fvWekaSuperlative = new ArrayList<String>();
		fvWekaSuperlative.add("Superlative");
		fvWekaSuperlative.add("NoSuperlative");
		attribute = new Attribute("Superlative", fvWekaSuperlative);
	}
	
	@Override
	public Object analyze(String q) {
		ComparisonUtils comp = new ComparisonUtils();
		if(comp.getSuperlatives(q).size() > 0) {
			return "Superlative";
		} else {
			return "NoSuperlative";
		}
	}
	public Attribute getAttribute() {
		return attribute;
	}

}
