package org.aksw.qa.annotation.index;

import java.util.ArrayList;
import java.util.List;

import org.aksw.qa.annotation.sparql.SimpleQuantityRanker;
import org.junit.Assert;
import org.junit.Test;

public class IndexDBO_classesTest {
	@Test
	public void correctOutputTypeTest() {
		IndexDBO classes = new IndexDBO_classes();
		List<String> out = classes.search("member");
		Assert.assertTrue("Return for member shoudnt be empty", !out.isEmpty());
		SimpleQuantityRanker ranker = new SimpleQuantityRanker();
		Assert.assertTrue("Returned uri is not a dbpedia class", ranker.disambiguateOntologyIsClass(out.get(0)));
	}

	@Test
	public void simpleTest() {
		IndexDBO_classes classes = new IndexDBO_classes();
		ArrayList<String> realPresidentResultList = new ArrayList<>();
		realPresidentResultList.add("http://dbpedia.org/ontology/President");
		realPresidentResultList.add("http://dbpedia.org/ontology/VicePresident");

		List<String> foundPresidentResultList = classes.search("president");
		Assert.assertTrue(realPresidentResultList.equals(foundPresidentResultList));
	}
}
