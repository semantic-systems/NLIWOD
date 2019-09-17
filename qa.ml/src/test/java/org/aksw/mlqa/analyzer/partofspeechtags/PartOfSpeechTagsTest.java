package org.aksw.mlqa.analyzer.partofspeechtags;

import static org.junit.Assert.assertArrayEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class PartOfSpeechTagsTest {

	@SuppressWarnings("unchecked")
	@Test
	public void tagsTest() {
		PartOfSpeechTags dep = new PartOfSpeechTags();
		Map<String,Integer> pos = (Map<String, Integer>) dep.analyze("What is the capital of Germany?");

		int[] actuals = new int[pos.keySet().size()];
		List<String> keyList = new ArrayList<String>(pos.keySet());
		for(int i = 0; i<keyList.size(); i++) {
			actuals[i] = pos.get(keyList.get(i));
		}

		int[] expecteds = {0, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 0};	
		assertArrayEquals(actuals,expecteds);
	}
}
