package org.aksw.mlqa.analyzer.entityType;

import java.util.List;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.experiment.SimpleClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.simple.Sentence;
import weka.core.Attribute;
import weka.core.FastVector;

public class EntityMoney implements IAnalyzer {
		static Logger log = LoggerFactory.getLogger(SimpleClassification.class);
		private Attribute attribute = null;
		
		public EntityMoney() {
			FastVector fvWekaMoney = new FastVector(2);
			fvWekaMoney.addElement("containsMoney");
			fvWekaMoney.addElement("containsNoMoney");
			attribute = new Attribute("Money", fvWekaMoney);
		}

		@Override
		public Object analyze(String q) {
			Sentence sent = new Sentence(q);
			List<String> nerTags = sent.nerTags();
			if(nerTags.contains("MONEY"))
				return "containsMoney";
			else
				return "containsNoMoney";
		}

		@Override
		public Attribute getAttribute() {
			return attribute;
		}
	}
