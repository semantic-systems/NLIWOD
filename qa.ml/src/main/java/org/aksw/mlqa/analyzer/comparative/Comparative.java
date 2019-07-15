package org.aksw.mlqa.analyzer.comparative;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.qa.annotation.comparison.ComparisonUtils;

import weka.core.Attribute;

/***
 * Analyzes if there is a comparative in the input question.
 * @author Lukas
 *
 */
public class Comparative implements IAnalyzer {
	private Attribute attribute = null;

	public Comparative(){
		ArrayList<String> fvWekaComparative = new ArrayList<String>();
		fvWekaComparative.add("Comparative");
		fvWekaComparative.add("NoComparative");
		attribute = new Attribute("Comparative", fvWekaComparative);
	}
		
	@Override
	public Object analyze(String q) {
		ComparisonUtils comp = new ComparisonUtils();
		if(comp.getComparatives(q).size() > 0) {
			return "Comparative";
		} else {
			return "NoComparative";
		}
	}
	
	public Attribute getAttribute() {
		return attribute;
	}
}
