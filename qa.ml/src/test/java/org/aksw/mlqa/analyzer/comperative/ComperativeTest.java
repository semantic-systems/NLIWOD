package org.aksw.mlqa.analyzer.comperative;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import weka.core.DenseInstance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ComperativeTest {

	@Test
	public void ThisContainsComperative() {
		Comperative superlative = new Comperative();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(superlative.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This test class is worse than the other!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("Comperative"));
	}

	@Test
	public void ThisContainsNoComperative(){
		Comperative superlative = new Comperative();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(superlative.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This sentence contains no comperative!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("NoComperative"));
	}

}