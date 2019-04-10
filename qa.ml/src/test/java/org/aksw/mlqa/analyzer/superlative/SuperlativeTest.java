package org.aksw.mlqa.analyzer.superlative;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class SuperlativeTest {

	@Test
	public void thisContainsSuperlative() {
		Superlative superlative = new Superlative();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(superlative.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This is the best test class!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("Superlative"));
	}

	@Test
	public void thisContainsNoSuperlative() {
		Superlative superlative = new Superlative();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(superlative.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This sentence contains no superlative!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("NoSuperlative"));
	}

}
