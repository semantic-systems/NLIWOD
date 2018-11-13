package org.aksw.mlqa.analyzer.numberOfToken;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;

//TODO write unit test for this analyzer
/**
 * Analyses the number of token in input question. TODO: Extract noun phrases
 * and count as one, e.g., "Where was ---Prince Charles--- born?"  4
 * 
 * @author ricardousbeck
 *
 */
public class NumberOfToken implements IAnalyzer {
	private Logger log = LoggerFactory.getLogger(NumberOfToken.class);
	private Attribute attribute = null;

	public NumberOfToken() {

		attribute = new Attribute("NumberOfToken");
	}

	@Override
	public Object analyze(String q) {
		log.debug("String question: " + q);
		String[] split = q.split("\\s+");
		return (double) split.length;
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
