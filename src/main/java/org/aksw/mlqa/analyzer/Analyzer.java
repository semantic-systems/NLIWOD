package org.aksw.mlqa.analyzer;

import java.util.ArrayList;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

public class Analyzer {

	private ArrayList<IAnalyzer> analyzers;
	private Attribute fmeasureAtt = new Attribute("fmeasure");
	public FastVector fvWekaAttributes = new FastVector();

	/**
	 *
	 * @param ClassAttribute
	 *            classes to be differentiated FastVector fvClassVal = new
	 *            FastVector(2); fvClassVal.addElement("positive");
	 *            fvClassVal.addElement("negative");
	 */
	public Analyzer() {

		analyzers = new ArrayList<IAnalyzer>();
		// !!! ADD ANALYZERS HERE !!!
		analyzers.add(new QuestionTypeAnalyzer());
		analyzers.add(new QueryResourceTypeAnalyzer());
		// Declare the feature vector, register their attributes
		for (IAnalyzer analyzer : analyzers) {
			fvWekaAttributes.addElement(analyzer.getAttribute());
		}
		// put the fmeasure/class attribute
		fvWekaAttributes.addElement(fmeasureAtt);
	}

	/**
	 *
	 * @param q
	 * @return feature vector leaving out a slot for the class variable, i.e.,
	 *         the QA system that can answer this feature vector
	 */
	public Instance analyze(String q) {
		// +1 to later add class attribute
		Instance tmpInstance = new Instance(fvWekaAttributes.size());
		// the feature adds itself to the instance
		for (IAnalyzer analyzer : analyzers) {
			Attribute attribute = analyzer.getAttribute();
			if (attribute.isNumeric()) {
				tmpInstance.setValue(attribute, (double) analyzer.analyze(q));
			} else if (attribute.isNominal() || attribute.isString()) {
				tmpInstance.setValue(attribute, (String) analyzer.analyze(q));
			}
		}

		return tmpInstance;

	}

	public Attribute getClassAttribute() {
		return fmeasureAtt;
	}

}