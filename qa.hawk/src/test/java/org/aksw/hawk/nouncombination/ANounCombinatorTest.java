package org.aksw.hawk.nouncombination;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.qa.commons.datastructure.Entity;

public abstract class ANounCombinatorTest {

	protected static boolean containsExactly(final List<Entity> list, final String... combinedNNs) {
		ArrayList<String> entityStrings = new ArrayList<>();
		for (Entity it : list) {
			entityStrings.add(it.getLabel());
		}
		ArrayList<String> combined = new ArrayList<>(Arrays.asList(combinedNNs));

		return combined.containsAll(entityStrings) && entityStrings.containsAll(combined);

	}
}
