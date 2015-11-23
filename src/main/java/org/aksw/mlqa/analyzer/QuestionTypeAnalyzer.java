package org.aksw.mlqa.analyzer;

import weka.core.Attribute;
import weka.core.FastVector;

public class QuestionTypeAnalyzer implements IAnalyzer {

	@Override
	public Object analyze(String q) {
		// FIXME write analysis step
		return QuestionTypeFeature.BOOLEAN.name();
	}

	@Override
	public Attribute getAttribute() {
		return QuestionTypeFeature.attribute;
	}

	// Enumeration values
	enum QuestionTypeFeature {
		LIST, NUMBER, BOOLEAN, RESOURCE;
		private static Attribute attribute = null;
		static {
			FastVector attributeValues = new FastVector(QuestionTypeFeature.values().length);
			for (QuestionTypeFeature qtf : QuestionTypeFeature.values()) {
				attributeValues.addElement(qtf.name());
			}
			attribute = new Attribute("QuestionTypeFeature", attributeValues);
		};
	}

}
