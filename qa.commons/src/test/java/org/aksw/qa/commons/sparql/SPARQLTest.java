package org.aksw.qa.commons.sparql;

import java.util.concurrent.ExecutionException;

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
		try {
			realAnswer = new ThreadedSPARQL(5, SPARQLEndpoints.DBPEDIA_ORG).sparql(query).toString();
		} catch (ExecutionException e) {
			e.printStackTrace();
			Assert.fail();
		}
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

}
