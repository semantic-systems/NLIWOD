package org.aksw.mlqa.features;

import weka.core.Instance;

public interface IFeature {
	public void addToInstance(Instance i);
}
