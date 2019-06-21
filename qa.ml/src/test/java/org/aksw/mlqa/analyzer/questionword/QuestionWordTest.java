package org.aksw.mlqa.analyzer.questionword;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class QuestionWordTest {
	
	private static String Commands = "Give||Show||List";
	
	private String AuxVerb = "Is||Are||Did||Does||Was||Do";
	
	@Test
	public void generalWordTest() {
		QuestionWord questionWord = new QuestionWord();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(questionWord.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(questionWord.getAttribute(), (String) questionWord.analyze("How high is the lighthouse in Colombo?"));	
		assertTrue(test.stringValue(questionWord.getAttribute()).equals("How"));
	}
	
	@Test
	public void commandWordTest() {
		QuestionWord questionWord = new QuestionWord();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(questionWord.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(questionWord.getAttribute(), (String) questionWord.analyze("List all the musicals with music by Elton John."));	
		assertTrue(test.stringValue(questionWord.getAttribute()).equals(Commands));
	}
	
	@Test
	public void auxWordTest() {
		QuestionWord questionWord = new QuestionWord();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(questionWord.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(questionWord.getAttribute(), (String) questionWord.analyze("Did Arnold Schwarzenegger attend a university?"));	
		assertTrue(test.stringValue(questionWord.getAttribute()).equals(AuxVerb));
	}
}
