package org.aksw.mlqa.features;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

//Enumeration values
public enum QuestionTypeFeature implements IFeature {
	LIST, NUMBER, BOOLEAN, RESOURCE;
	private static Attribute attribute = null;
	static {
		FastVector attributeValues = new FastVector(QuestionTypeFeature.values().length);
		for (QuestionTypeFeature qtf : QuestionTypeFeature.values()) {
			attributeValues.addElement(qtf.name());
		}
		attribute = new Attribute("QuestionTypeFeature", attributeValues);
	};

	@Override
	public void addToInstance(Instance i) {
		System.out.println(attribute.enumerateValues());
		i.setValue(attribute, this.name());
	}

}
