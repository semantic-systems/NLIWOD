package org.aksw.mlqa;

import java.util.ArrayList;
import java.util.List;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.analyzer.QuestionTypeAnalyzer;
import org.aksw.mlqa.features.IFeature;

import weka.core.Instance;

public class Analyzer {

	private ArrayList<IAnalyzer> analyzers;

	public Analyzer() {

		analyzers = new ArrayList<IAnalyzer>();
		// Add analyzers here
		analyzers.add(new QuestionTypeAnalyzer());
	}

	// produces a feature instance for each question
	public Instance analyze(String q) {
		List<IFeature> tmpList = new ArrayList<IFeature>();

		// calculate every feature based on analyzers
		for (IAnalyzer a : analyzers) {
			List<IFeature> features = a.analyze(q);
			if (features != null) {
				tmpList.addAll(features);
			}
		}

		Instance tmpInstance = new Instance(tmpList.size());
		// the feature adds itself to the instance
		for (IFeature feature : tmpList) {
			feature.addToInstance(tmpInstance);
		}

		return tmpInstance;

	}

	public static void main(String[] args) {

		Analyzer f = new Analyzer();

		// input question
		String q = "What is the capital of Germany?";

		// calculate features
		Instance tmp = f.analyze(q);

		// output feature vector
		System.out.println(tmp);
	}
}
