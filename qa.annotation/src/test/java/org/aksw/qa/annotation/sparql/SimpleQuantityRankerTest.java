package org.aksw.qa.annotation.sparql;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class SimpleQuantityRankerTest {
	private SimpleQuantityRanker ranker = new SimpleQuantityRanker();

	@Test
	public void test() {

		ArrayList<String> uris = new ArrayList<>();
		uris.add("http://dbpedia.org/ontology/Person");
		uris.add("http://dbpedia.org/ontology/Animal");

		Assert.assertTrue(ranker.rank(uris).equals(uris.get(0)));

	}
}
