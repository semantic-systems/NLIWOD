package org.aksw.mlqa.analyzer.comperative;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.qa.annotation.comparison.ComparisonUtils;

import weka.core.Attribute;

public class Comperative implements IAnalyzer {
	//static Logger log = LoggerFactory.getLogger(Comperative.class);
	private Attribute attribute = null;

	public Comperative(){
		ArrayList<String> fvWekaComperative = new ArrayList<String>();
		fvWekaComperative.add("Comperative");
		fvWekaComperative.add("NoComperative");
		attribute = new Attribute("Comperative", fvWekaComperative);
	}
		
	@Override
	public Object analyze(String q) {
		ComparisonUtils comp = new ComparisonUtils();
		if(comp.getComparatives(q).size() > 0) {
			return "Comperative";
		} else {
			return "NoComperative";
		}
	}
	public Attribute getAttribute() {
		return attribute;
	}

}
