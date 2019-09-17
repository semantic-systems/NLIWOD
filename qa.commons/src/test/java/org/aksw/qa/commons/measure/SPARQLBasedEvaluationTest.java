package org.aksw.qa.commons.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.aksw.qa.commons.sparql.SPARQL;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLBasedEvaluationTest {
	private String endpoint = "http://dbpedia.org/sparql";
	private Logger logger = LoggerFactory.getLogger(SPARQLBasedEvaluationTest.class);

	@Test
	public void testEndpointAvailibility() {
		assertTrue(SPARQL.isEndpointAlive("http://dbpedia.org/sparql"));
		assertFalse(SPARQL.isEndpointAlive("http://dbpedia2.org/sparql"));
	}

	@Test
	public void testTooGenericQuery() {
		String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts .}";
		String targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts ." + "?uri dbo:director res:Joel_Schumacher .}";
		double precision = SPARQLBasedEvaluation.precision(sparqlQuery, targetSPARQLQuery, endpoint);
		double recall = SPARQLBasedEvaluation.recall(sparqlQuery, targetSPARQLQuery, endpoint);
		double fMeasure = SPARQLBasedEvaluation.fMeasure(sparqlQuery, targetSPARQLQuery, endpoint);
		assertEquals(0.05, precision, 0.001);
		assertEquals(1.0, recall, 0.0);
		assertEquals(0.095, fMeasure, 0.001);

		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		logger.debug("P=" + precision);
		logger.debug("R=" + recall);
		logger.debug("F=" + fMeasure);

	}

	@Test
	public void testTooSpecificQuery() {
		// SELECT COUNT(?x)...
		String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT (COUNT(DISTINCT ?uri) as ?u) WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts .}";
		String targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT (COUNT(DISTINCT ?uri) as ?u) WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts ." + "?uri dbo:director res:Garry_Marshall .}";
		double precision = SPARQLBasedEvaluation.precision(sparqlQuery, targetSPARQLQuery, endpoint);
		double recall = SPARQLBasedEvaluation.recall(sparqlQuery, targetSPARQLQuery, endpoint);
		double fMeasure = SPARQLBasedEvaluation.fMeasure(sparqlQuery, targetSPARQLQuery, endpoint);

		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		logger.debug("P=" + precision);
		logger.debug("R=" + recall);
		logger.debug("F=" + fMeasure);
	}

}
