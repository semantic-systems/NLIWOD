package org.aksw.mlqa.analyzer;

import weka.core.Attribute;

public interface IAnalyzer {
	/**
	 *
	 * @param q
	 * @return
	 */
	public Object analyze(String q);

	public Attribute getAttribute();
}
