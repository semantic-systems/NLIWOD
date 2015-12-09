package org.aksw.mlqa.analyzer;

import weka.core.Attribute;
import weka.core.Instance;

public interface IAnalyzer {
	/**
	 *
	 * @param q
	 * @return
	 */
	public Object analyze(String q);

	public Attribute getAttribute();
}
