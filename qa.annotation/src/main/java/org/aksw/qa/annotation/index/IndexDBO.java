package org.aksw.qa.annotation.index;

import java.util.List;

import com.google.common.collect.ImmutableList;

public abstract class IndexDBO {
	protected static List<String> stopwords = ImmutableList.of("the", "of", "on", "in", "for", "at", "to");

	public abstract List<String> search(final String object);

	public abstract void close();
}