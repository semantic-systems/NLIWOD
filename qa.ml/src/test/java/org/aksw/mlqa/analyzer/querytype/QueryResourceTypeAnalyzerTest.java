package org.aksw.mlqa.analyzer.querytype;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class QueryResourceTypeAnalyzerTest {

	@Test
	public void dateTest() {
		QueryResourceTypeAnalyzer answerType = new QueryResourceTypeAnalyzer();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(answerType.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(answerType.getAttribute(), (String) answerType.analyze("When did Finland join the EU?"));
		assertTrue(testinstance.stringValue(answerType.getAttribute()).equals("Schema:Date"));
	}
	
	@Test
	public void personTest() {
		QueryResourceTypeAnalyzer answerType = new QueryResourceTypeAnalyzer();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(answerType.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(answerType.getAttribute(), (String) answerType.analyze("Who wrote Harry Potter?"));
		assertTrue(testinstance.stringValue(answerType.getAttribute()).equals("DBpedia:Person"));
	}
	
	@Test
	public void resourceTest() {
		QueryResourceTypeAnalyzer answerType = new QueryResourceTypeAnalyzer();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(answerType.getAttribute());
		new Instances("Test", fvWekaAttributes, 1);
		Instance testinstance = new DenseInstance(fvWekaAttributes.size());
		testinstance.setValue(answerType.getAttribute(), (String) answerType.analyze("What is the highest mountain in Germany?"));
		assertTrue(testinstance.stringValue(answerType.getAttribute()).equals("DBpedia:Mountain"));
	}
}
