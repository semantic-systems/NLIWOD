package org.aksw.qa.commons.sparql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.aksw.qa.commons.utils.Results;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLTest {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	
	@Test
	public void dbpediaTest() {
		log.debug("Trying to query dbpedia...(Timeout 5s)");
		String query = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?uri ?string WHERE { ?uri rdf:type dbo:FormulaOneRacer . ?uri dbo:races ?x . OPTIONAL { ?uri rdfs:label ?string. FILTER (lang(?string) = 'en') } } ORDER BY DESC(?x) OFFSET 0 LIMIT 1";
		String answer = "[http://dbpedia.org/resource/Michael_Schumacher]";
		String realAnswer = "";
		realAnswer = new ThreadedSPARQL(5, SPARQLEndpoints.DBPEDIA_ORG).sparql(query).toString();
		Assert.assertTrue("Answersets differ", answer.equals(realAnswer));
	}
	
	@Test
	public void wikidataTest() throws ExecutionException {
		log.debug("Trying to query wikidata...(Timeout 5s)");
		String query = "SELECT DISTINCT ?company WHERE {?company 		 <http://www.wikidata.org/prop/direct/P452> 		 <http://www.wikidata.org/entity/Q581105> ; 		 <http://www.wikidata.org/prop/direct/P740> 		 <http://www.wikidata.org/entity/Q956> . }";
		String answer = "[http://www.wikidata.org/entity/Q1636958]";
		String realAnswer = "";
		realAnswer = new SPARQL(SPARQLEndpoints.WIKIDATA_ORG).sparql(query).toString();
		Assert.assertTrue("Answersets differ", answer.equals(realAnswer));
	}
	
	@Test
	public void testEndpointAlive() {
		assertTrue(SPARQL.isEndpointAlive("http://dbpedia.org/sparql"));
		assertFalse(SPARQL.isEndpointAlive("http://dbpedia2.org/sparql"));
	}

	@Test
	public void testAsk() {
		assertFalse(SPARQL.executeAsk("PREFIX foaf:    <http://xmlns.com/foaf/0.1/> ASK  { ?x foaf:name  \"Alice\" }", "http://dbpedia.org/sparql"));
		assertTrue(SPARQL.executeAsk("PREFIX foaf:    <http://xmlns.com/foaf/0.1/> ASK  { ?x foaf:name  \"Academy Award\"@en }", "http://dbpedia.org/sparql"));
	}

	@Test
	public void testSelect() {
		Set<String> resources = SPARQL.executeSelect("SELECT ?s FROM <http://dbpedia.org> {?s ?p ?o} LIMIT 3", "http://dbpedia.org/sparql").getStringSet();
		Object[] actual = resources.toArray();
		Arrays.sort(actual);
		assertArrayEquals(getGoldenArray1(), actual);
		
		resources = SPARQL.executeSelect("SELECT ?o FROM <http://dbpedia.org> {?s <http://dbpedia.org/ontology/birthDate> ?o} LIMIT 3 OFFSET 100", "http://dbpedia.org/sparql").getStringSet();
		actual = resources.toArray();
		Arrays.sort(actual);
		assertArrayEquals(getGoldenArray2(), actual);
	}

	@Test
	public void testResultsSet() {
		Results res = SPARQL.executeSelect("PREFIX foaf:    <http://xmlns.com/foaf/0.1/> SELECT ?s ?o FROM <http://dbpedia.org> {?s foaf:name ?o} LIMIT 2", "http://dbpedia.org/sparql");
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
		Results res = SPARQL.executeSelect("SELECT ?s ?o FROM <http://dbpedia.org> {?s ?p ?o} LIMIT 2", "http://dbpedia.org/sparql");
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
		ret[1] = "\"1785-03-07\"^^<http://www.w3.org/2001/XMLSchema#date>";
		ret[2] = "\"1950-1-9\"^^<http://www.w3.org/2001/XMLSchema#date>";
		Arrays.sort(ret);
		return ret;
	}
}
