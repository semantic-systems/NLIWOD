package org.aksw.mlqa.analyzer.dependencies;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class DependenciesTest {

	@SuppressWarnings("unchecked")
	@Test
	public void dependenciesTest() {
		Dependencies dep = new Dependencies();
		Map<String,Integer> dependencies = (Map<String, Integer>) dep.analyze("What is the capital of Germany?");

		int[] actuals = new int[dependencies.keySet().size()];
		List<String> keyList = new ArrayList<String>(dependencies.keySet());
		for(int i = 0; i<keyList.size(); i++) {
			actuals[i] = dependencies.get(keyList.get(i));
		}
		
		int[] expecteds = {1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 1};	
		assertArrayEquals(actuals,expecteds);
	}
}
