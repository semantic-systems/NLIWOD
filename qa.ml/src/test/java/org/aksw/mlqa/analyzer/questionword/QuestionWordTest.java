package org.aksw.mlqa.analyzer.questionword;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class QuestionWordTest {
	
	private String Commands = "Give||Show||List";
	
	@Test
	public void questionWordTest() {
		QuestionWord questionWord = new QuestionWord();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(questionWord.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(questionWord.getAttribute(), (String) questionWord.analyze("How high is the lighthouse in Colombo?"));	
		assertTrue(test.stringValue(questionWord.getAttribute()).equals("How"));
		
		test.setValue(questionWord.getAttribute(), (String) questionWord.analyze("List all the musicals with music by Elton John."));	
		assertTrue(test.stringValue(questionWord.getAttribute()).equals(Commands));	
	}
}
