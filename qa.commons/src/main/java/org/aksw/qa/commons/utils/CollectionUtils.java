package org.aksw.qa.commons.utils;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 *
 * @author felixconrads (tortugaattack)
 *
 */

public class CollectionUtils {
	// But why?
	public static <E> HashSet<E> newHashSet() {
		return new HashSet<>();
	}

	public static <K, V> LinkedHashMap<K, V> newLinkedHashMap() {
		return new LinkedHashMap<>();
	}

	public static <E> Set<E> intersection(final Set<E> arg1, final Set<E> arg2) {

		Set<E> intersect = new HashSet<>(arg1);
		intersect.retainAll(arg2);
		return intersect;

	}

}
