package org.aksw.mlqa.analyzer.numberoftoken;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.numberoftoken.NumberOfToken;
import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class NumberOfTokenTest {
	
	@Test
	public void numberTokensTest() {
		NumberOfToken numberOfToken = new NumberOfToken();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(numberOfToken.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(numberOfToken.getAttribute(), (double) numberOfToken.analyze("This test is worse than the other!"));
		assertTrue(test.value(numberOfToken.getAttribute()) == 7.0);
	}
	
	@Test
	public void nounPhraseTest() {
		NumberOfToken numberOfToken = new NumberOfToken();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(numberOfToken.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(numberOfToken.getAttribute(), (double) numberOfToken.analyze("Where was Prince Charles born?"));
		assertTrue(test.value(numberOfToken.getAttribute()) == 4.0);

		test.setValue(numberOfToken.getAttribute(), (double) numberOfToken.analyze("The Delta Airlines flight to New York is ready to board."));
		assertTrue(test.value(numberOfToken.getAttribute()) == 8.0);		
	}
}
