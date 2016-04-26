package org.aksw.mlqa.analyzer.entityType;

import java.util.List;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.experiment.SimpleClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.simple.Sentence;
import weka.core.Attribute;
import weka.core.FastVector;

public class EntityPerson implements IAnalyzer {
	static Logger log = LoggerFactory.getLogger(SimpleClassification.class);
	private Attribute attribute = null;
	
	public EntityPerson() {
		FastVector fvWekaPerson = new FastVector(2);
		fvWekaPerson.addElement("containsPerson");
		fvWekaPerson.addElement("containsNoPerson");
		attribute = new Attribute("Person", fvWekaPerson);
	}

	@Override
	public Object analyze(String q) {
		Sentence sent = new Sentence(q);
		List<String> nerTags = sent.nerTags();
		if(nerTags.contains("PERSON"))
			return "containsPerson";
		else
			return "containsNoPerson";
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}

}
