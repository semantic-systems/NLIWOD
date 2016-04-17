package org.aksw.qa.commons.utils;

import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionsTest {
	Logger log = LoggerFactory.getLogger(CollectionsTest.class);

	@Test
	public void testIntersect() {
		// {a, b, c, d, e, abc, def}
		Set<String> set1 = new HashSet<String>();
		set1.add("a");
		set1.add("b");
		set1.add("c");
		set1.add("d");
		set1.add("e");
		set1.add("abc");
		set1.add("def");

		// {a, d, abc, dfe}
		Set<String> set2 = new HashSet<String>();
		set2.add("a");
		set2.add("d");
		set2.add("abc");
		set2.add("dfe");

		Set<String> expected = new HashSet<String>();
		expected.add("a");
		expected.add("d");
		expected.add("abc");

		// {a, d, abc}
		Set<String> intersect = CollectionUtils.intersection(set1, set2);

		log.debug("Set1 : " + set1);
		log.debug("Set2 : " + set2);
		log.debug("Intersect : " + intersect);

		Object[] actual = intersect.toArray();
		Object[] expectedA = expected.toArray();
		Arrays.sort(actual);
		Arrays.sort(expectedA);

		assertArrayEquals(expectedA, actual);
	}
}
