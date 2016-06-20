package org.aksw.mlqa.analyzer;

import weka.core.Attribute;

public interface IAnalyzer {
	public Object analyze(String q);

	public Attribute getAttribute();
}
