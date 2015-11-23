package org.aksw.mlqa;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.analyzer.QuestionTypeAnalyzer;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class Analyzer {

	private ArrayList<IAnalyzer> analyzers;
	private FastVector fvWekaAttributes = new FastVector();

	/**
	 * 
	 * @param ClassAttribute
	 *            classes to be differentiated FastVector fvClassVal = new
	 *            FastVector(2); fvClassVal.addElement("positive");
	 *            fvClassVal.addElement("negative");
	 */
	public Analyzer(Attribute ClassAttribute) {

		analyzers = new ArrayList<IAnalyzer>();
		// Add analyzers here
		analyzers.add(new QuestionTypeAnalyzer());

		// Declare the feature vector, register their attributes
		for (IAnalyzer analyzer : analyzers) {
			fvWekaAttributes.addElement(analyzer.getAttribute());
		}
		// add class attribute
		fvWekaAttributes.addElement(ClassAttribute);
	}

	// produces a feature instance for each question
	public Instance analyze(String q) {

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

	public static void main(String[] args) {
		// Declare the class attribute along with its values
		FastVector fvClassVal = new FastVector(2);
		fvClassVal.addElement("positive");
		fvClassVal.addElement("negative");
		Attribute ClassAttribute = new Attribute("theClass", fvClassVal);

		Analyzer analyzer = new Analyzer(ClassAttribute);
		// Create an empty training set
		Instances isTrainingSet = new Instances("training", analyzer.fvWekaAttributes, 10);
		// Set class index
		isTrainingSet.setClass(ClassAttribute);

		// input question
		String q = "What is the capital of Germany?";

		// calculate features
		Instance tmp = analyzer.analyze(q);

		// output feature vector
		System.out.println(tmp);
	}
}
