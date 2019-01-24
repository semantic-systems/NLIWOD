package org.aksw.qa.commons.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;

public class SPARQLExecutorTest {

	@Test
	public void testEndpointAlive() {
		assertTrue(SPARQLExecutor.isEndpointAlive("http://dbpedia.org/sparql"));
		assertFalse(SPARQLExecutor.isEndpointAlive("http://dbpedia2.org/sparql"));
	}

	@Test
	public void testAsk() {
		assertFalse(SPARQLExecutor.executeAsk("PREFIX foaf:    <http://xmlns.com/foaf/0.1/> ASK  { ?x foaf:name  \"Alice\" }", "http://dbpedia.org/sparql"));
		assertTrue(SPARQLExecutor.executeAsk("PREFIX foaf:    <http://xmlns.com/foaf/0.1/> ASK  { ?x foaf:name  \"Academy Award\"@en }", "http://dbpedia.org/sparql"));
	}

	@Test
	public void testSelect() {
		Set<String> resources = SPARQLExecutor.executeSelect("SELECT ?s FROM <http://dbpedia.org> {?s ?p ?o} LIMIT 3", "http://dbpedia.org/sparql").getStringSet();
		Object[] actual = resources.toArray();
		Arrays.sort(actual);
		assertArrayEquals(getGoldenArray1(), actual);
		
		resources = SPARQLExecutor.executeSelect("SELECT ?o FROM <http://dbpedia.org> {?s <http://dbpedia.org/ontology/birthDate> ?o} LIMIT 3 OFFSET 100", "http://dbpedia.org/sparql").getStringSet();
		actual = resources.toArray();
		Arrays.sort(actual);
		assertArrayEquals(getGoldenArray2(), actual);
	}

	@Test
	public void testResultsSet() {
		Results res = SPARQLExecutor.executeSelect("PREFIX foaf:    <http://xmlns.com/foaf/0.1/> SELECT ?s ?o FROM <http://dbpedia.org> {?s foaf:name ?o} LIMIT 2", "http://dbpedia.org/sparql");
		String[] expecteds = new String[4];
		expecteds[0] = "http://dbpedia.org/resource/Academy_Award_(radio)";
		expecteds[1] = "\"Academy Award\"@en";
		expecteds[2] = "http://dbpedia.org/resource/Aggregation_(magazine)";
		expecteds[3] = "\"Aggregation\"@en";
		Object[] actuals = res.getStringSet().toArray();
		Arrays.sort(expecteds);
		Arrays.sort(actuals);
		assertArrayEquals(expecteds, actuals);
	}

	@Test
	public void testResults() {
		Results res = SPARQLExecutor.executeSelect("SELECT ?s ?o FROM <http://dbpedia.org> {?s ?p ?o} LIMIT 2", "http://dbpedia.org/sparql");
		assertTrue(res.header.size() == 2);
		assertTrue(res.header.contains("s"));
		assertTrue(res.header.contains("o"));
		List<String> row = res.table.get(0);

		assertTrue(row.get(res.header.indexOf("o")).equals("http://www.w3.org/2002/07/owl#FunctionalProperty"));
		assertTrue(row.get(res.header.indexOf("s")).equals("http://dbpedia.org/ontology/deathDate"));
		row = res.table.get(1);
		assertTrue(row.get(res.header.indexOf("o")).equals("http://www.w3.org/2002/07/owl#FunctionalProperty"));
		assertTrue(row.get(res.header.indexOf("s")).equals("http://dbpedia.org/ontology/birthDate"));
	}

	private Object[] getGoldenArray1() {
		String[] ret = new String[3];
		ret[0] = "http://dbpedia.org/ontology/birthDate";
		ret[1] = "http://dbpedia.org/ontology/acceleration";
		ret[2] = "http://dbpedia.org/ontology/deathDate";
		Arrays.sort(ret);
		return ret;
	}

	private Object[] getGoldenArray2() {
		String[] ret = new String[3];
		ret[0] = "\"1785-3-7\"^^<http://www.w3.org/2001/XMLSchema#date>";
		ret[1] = "1785-03-07";
		ret[2] = "\"1950-1-9\"^^<http://www.w3.org/2001/XMLSchema#date>";
		Arrays.sort(ret);
		return ret;
	}

}
