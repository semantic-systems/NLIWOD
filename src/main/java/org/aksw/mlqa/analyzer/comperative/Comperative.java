package org.aksw.mlqa.analyzer.comperative;

import java.util.List;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.experiment.SimpleClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.simple.Sentence;
import weka.core.Attribute;
import weka.core.FastVector;

public class Comperative implements IAnalyzer {
	static Logger log = LoggerFactory.getLogger(SimpleClassification.class);
	private Attribute attribute = null;

	public Comperative(){
		FastVector attributeValues = new FastVector();
		attributeValues.addElement("containsComperative");
		attributeValues.addElement("containsNoComperative");
		
		attribute = new Attribute("Comperative", attributeValues);

	}

	public Object analyze(String q) {
		Sentence sent = new Sentence(q);
		List<String> posTags = sent.posTags();
		if(posTags.contains("JJR")||posTags.contains("RBR"))
			return "containsComperative";
		else
			return "containsNoComperative";
	}

	public Attribute getAttribute() {
		return attribute;
	}

}
