package org.aksw.qa.annotation.index;

import java.util.List;

import org.aksw.qa.annotation.sparql.SimpleQuantityRanker;
import org.junit.Assert;
import org.junit.Test;

public class IndexDBO_propertiesTest {
	@Test
	public void correctOutputTypeTest() {
		IndexDBO properties = new IndexDBO_properties();
		List<String> out = properties.search("member");
		Assert.assertTrue("Return for member shoudnt be empty", !out.isEmpty());
		SimpleQuantityRanker ranker = new SimpleQuantityRanker();
		Assert.assertTrue("Returned uri is not a dbpedia property: |" + out.get(0) + "|", ranker.disambiguateOntologyIsProperty(out.get(0)));
	}

	@Test
	public void simpleTest() {
		IndexDBO properties = new IndexDBO_properties();
		String spouse = "http://dbpedia.org/ontology/spouse";

		Assert.assertTrue(properties.search("husband").get(0).equals(spouse));
		Assert.assertTrue(properties.search("wife").get(0).equals(spouse));
	}
}
