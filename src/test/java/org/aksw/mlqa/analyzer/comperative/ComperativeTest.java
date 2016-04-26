package org.aksw.mlqa.analyzer.comperative;

import static org.junit.Assert.*;

import org.junit.Test;

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
		Instance test = new Instance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This test class is worse than the other!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("containsComperative"));
	}

	@Test
	public void ThisContainsNoComperative(){
		Comperative superlative = new Comperative();
		FastVector fvWekaAttributes = new FastVector();
		fvWekaAttributes.addElement(superlative.getAttribute());
		Instances testinstances = new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new Instance(fvWekaAttributes.size());
		test.setValue(superlative.getAttribute(), (String) superlative.analyze("This sentence contains no comperative!"));		
		assertTrue(test.stringValue(superlative.getAttribute()).equals("containsNoComperative"));
	}

}