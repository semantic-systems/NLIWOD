package org.aksw.mlqa.analyzer.questiontype;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class QuestionTypeAnalyzerTest {
	
	@Test
	public void resultTypeResource() {
		QuestionTypeAnalyzer typeAnalyzer = new QuestionTypeAnalyzer();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(typeAnalyzer.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(typeAnalyzer.getAttribute(), (String) typeAnalyzer.analyze("Who was the doctoral supervisor of Albert Einstein?"));	
		assertTrue(test.stringValue(typeAnalyzer.getAttribute()).equals("RESOURCE"));
	}
	
	@Test
	public void resultTypeBoolean() {
		QuestionTypeAnalyzer typeAnalyzer = new QuestionTypeAnalyzer();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(typeAnalyzer.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(typeAnalyzer.getAttribute(), (String) typeAnalyzer.analyze("Does Neymar play for Real Madrid?"));	
		assertTrue(test.stringValue(typeAnalyzer.getAttribute()).equals("BOOLEAN"));
	}
}
