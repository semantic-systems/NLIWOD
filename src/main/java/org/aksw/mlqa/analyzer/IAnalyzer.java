package org.aksw.mlqa.analyzer;

import java.util.List;

import org.aksw.mlqa.features.IFeature;

public interface IAnalyzer {
	/**
	 * 
	 * @param q
	 * @return
	 */
	public List<IFeature> analyze(String q);
}
