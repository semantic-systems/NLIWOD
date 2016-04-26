package org.aksw.mlqa.analyzer.entityType;

import java.util.List;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.experiment.SimpleClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.simple.Sentence;
import weka.core.Attribute;
import weka.core.FastVector;

public class EntityTime implements IAnalyzer {
	static Logger log = LoggerFactory.getLogger(SimpleClassification.class);
	private Attribute attribute = null;
	
	public EntityTime() {
		FastVector fvWekaTime= new FastVector(2);
		fvWekaTime.addElement("containsTime");
		fvWekaTime.addElement("containsNoTime");
		attribute = new Attribute("Time", fvWekaTime);
	}
		
	@Override
	public Object analyze(String q) {
		Sentence sent = new Sentence(q);
		List<String> nerTags = sent.nerTags();
		log.debug(nerTags.toString());
		if(nerTags.contains("TIME"))
			return "containsNoTime";
		else
			return "containsNoTime";
	}
		
	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
