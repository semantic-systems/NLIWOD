package org.aksw.mlqa.analyzer.entityType;

import java.util.List;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.experiment.SimpleClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.simple.Sentence;
import weka.core.Attribute;
import weka.core.FastVector;


public class EntityOrganization implements IAnalyzer {
		static Logger log = LoggerFactory.getLogger(SimpleClassification.class);
		private Attribute attribute = null;
		
		public EntityOrganization() {
			FastVector fvWekaOrganization = new FastVector(2);
			fvWekaOrganization.addElement("containsOrganization");
			fvWekaOrganization.addElement("containsNoOrganization");
			attribute = new Attribute("Location", fvWekaOrganization);
		}

		@Override
		public Object analyze(String q) {
			Sentence sent = new Sentence(q);
			List<String> nerTags = sent.nerTags();
			if(nerTags.contains("ORGANIZATION"))
				return "containsOrganization";
			else
				return "containsNoOrganization";
		}

		@Override
		public Attribute getAttribute() {
			return attribute;
		}
	}

