package org.aksw.mlqa.analyzer.entityType;

import java.util.List;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.experiment.SimpleClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.simple.Sentence;
import weka.core.Attribute;
import weka.core.FastVector;

public class EntityLocation implements IAnalyzer {
	static Logger log = LoggerFactory.getLogger(SimpleClassification.class);
	private Attribute attribute = null;
	
	public EntityLocation() {
		FastVector fvWekaLocation = new FastVector(2);
		fvWekaLocation.addElement("containsLocation");
		fvWekaLocation.addElement("containsNoLocation");
		attribute = new Attribute("Location", fvWekaLocation);
	}

	@Override
	public Object analyze(String q) {
		Sentence sent = new Sentence(q);
		List<String> nerTags = sent.nerTags();
		if(nerTags.contains("LOCATION"))
			return "containsLocation";
		else
			return "containsNoLocation";
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
