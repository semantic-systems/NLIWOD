package org.aksw.mlqa.analyzer.comperative;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class ComperativeTest {

	@Test
	public void thisContainsComperative() {
		Comperative superlative = new Comperative();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(superlative.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This test class is worse than the other!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("Comperative"));
	}

	@Test
	public void thisContainsNoComperative(){
		Comperative superlative = new Comperative();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(superlative.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This sentence contains no comperative!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("NoComperative"));
	}

}