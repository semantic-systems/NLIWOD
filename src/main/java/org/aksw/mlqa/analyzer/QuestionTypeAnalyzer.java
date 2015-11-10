package org.aksw.mlqa.analyzer;

import java.util.ArrayList;
import java.util.List;

import org.aksw.mlqa.features.IFeature;
import org.aksw.mlqa.features.QuestionTypeFeature;

public class QuestionTypeAnalyzer implements IAnalyzer {

	@Override
	public List<IFeature> analyze(String q) {

		// returns List, Resource, Boolean, Date
		List<IFeature> tmpFeature = new ArrayList<IFeature>();
		// tmpFeature.add(new QuestionTypeFeature("Date"));
		// tmpFeature.add(new QuestionTypeFeature("Resource"));
		tmpFeature.add(QuestionTypeFeature.BOOLEAN);

		return tmpFeature;
	}
}
