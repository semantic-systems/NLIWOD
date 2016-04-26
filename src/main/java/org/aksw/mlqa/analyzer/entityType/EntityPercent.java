package org.aksw.mlqa.analyzer.entityType;

import java.util.List;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.experiment.SimpleClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.simple.Sentence;
import weka.core.Attribute;
import weka.core.FastVector;

public class EntityPercent implements IAnalyzer {
		static Logger log = LoggerFactory.getLogger(SimpleClassification.class);
		private Attribute attribute = null;
		
		public EntityPercent() {
			FastVector fvWekaPercent = new FastVector(2);
			fvWekaPercent.addElement("containsPercent");
			fvWekaPercent.addElement("containsNoPercent");
			attribute = new Attribute("Percent", fvWekaPercent);
		}

		@Override
		public Object analyze(String q) {
			Sentence sent = new Sentence(q);
			List<String> nerTags = sent.nerTags();
			if(nerTags.contains("PERCENT"))
				return "containsPercent";
			else
				return "containsNoPercent";
		}

		@Override
		public Attribute getAttribute() {
			return attribute;
		}
	}

