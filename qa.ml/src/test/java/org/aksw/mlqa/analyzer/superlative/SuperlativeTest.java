package org.aksw.mlqa.analyzer.superlative;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class SuperlativeTest {

	@Test
	public void ThisContainsSuperlative() {
		Superlative superlative = new Superlative();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(superlative.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This is the best test class!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("Superlative"));
	}

	@Test
	public void ThisContainsNoSuperlative() {
		Superlative superlative = new Superlative();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(superlative.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This sentence contains no superlative!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("NoSuperlative"));
	}

}
