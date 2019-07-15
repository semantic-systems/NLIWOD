package org.aksw.mlqa.analyzer;

import weka.core.Attribute;

public interface IAnalyzer {
	
	/***
	 * Extracts a feature from the given question.
	 * @param q question
	 * @return feature value
	 */
	public Object analyze(String q);

	
	/***
	 * Returns the attribute of the IAnalyzer.
	 * @return attribute of the IAnalyzer
	 */
	public Attribute getAttribute();
}
