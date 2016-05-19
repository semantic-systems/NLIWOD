package org.aksw.hawk.cache;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.MutableTree;

public interface CachedParseTree {

	public MutableTree process(HAWKQuestion q);
}
