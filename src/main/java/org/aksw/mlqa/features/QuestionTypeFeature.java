package org.aksw.mlqa.features;

import weka.core.Attribute;
import weka.core.Instance;

//Enumeration values
public enum QuestionTypeFeature implements IFeature {
	DATE, BOOLEAN;
	private static Attribute attribute = new Attribute("QuestionTypeFeature");

	@Override
	public void addToInstance(Instance i) {
		i.setValue(attribute, name());
	}

}
