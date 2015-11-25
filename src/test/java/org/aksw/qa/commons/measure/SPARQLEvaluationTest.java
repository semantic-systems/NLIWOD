package org.aksw.qa.commons.measure;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLEvaluationTest {
	String endpoint = "http://dbpedia.org/sparql";
	Logger logger = LoggerFactory.getLogger(SPARQLEvaluationTest.class);

	// TODO add test whether endpoint is available
	@Test
	public void testTooGenericQuery() {
		String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts .}";
		String targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts ." + "?uri dbo:director res:Garry_Marshall .}";
		double precision = SPARQLEvaluation.precision(sparqlQuery, targetSPARQLQuery, endpoint);
		double recall = SPARQLEvaluation.recall(sparqlQuery, targetSPARQLQuery, endpoint);
		double fMeasure = SPARQLEvaluation.fMeasure(sparqlQuery, targetSPARQLQuery, endpoint);
		assertEquals(0.0571, precision, 0.001);
		assertEquals(1.0, recall, 0.0);
		assertEquals(0.108, fMeasure, 0.001);
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		logger.debug("P=" + precision);
		logger.debug("R=" + recall);
		logger.debug("F=" + fMeasure);

	}

	@Test
	public void testTooSpecificQuery() {
		// SELECT COUNT(?x)...
		String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT COUNT(DISTINCT ?uri) WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts .}";
		String targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT COUNT(DISTINCT ?uri) WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts ." + "?uri dbo:director res:Garry_Marshall .}";
		double precision = SPARQLEvaluation.precision(sparqlQuery, targetSPARQLQuery, endpoint);
		double recall = SPARQLEvaluation.recall(sparqlQuery, targetSPARQLQuery, endpoint);
		double fMeasure = SPARQLEvaluation.fMeasure(sparqlQuery, targetSPARQLQuery, endpoint);
		
		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		logger.debug("P=" + precision);
		logger.debug("R=" + recall);
		logger.debug("F=" + fMeasure); 
		
		//TODO also transform to unit test
		
		// String sparqlQuery =
									   // "PREFIX dbo: <http://dbpedia.org/ontology/> "
									   // +
		// "PREFIX res: <http://dbpedia.org/resource/> " +
		// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		// "SELECT DISTINCT ?uri WHERE {	" +
		// "?uri rdf:type dbo:Film ." +
		// "?uri dbo:starring res:Julia_Roberts .}";
		// String targetSPARQLQuery =
		// "PREFIX dbo: <http://dbpedia.org/ontology/> " +
		// "PREFIX res: <http://dbpedia.org/resource/> " +
		// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		// "SELECT DISTINCT ?uri WHERE {	" +
		// "?uri rdf:type dbo:Film ." +
		// "?uri dbo:starring res:Julia_Roberts ." +
		// "?uri dbo:director res:Garry_Marshall .}";
		// SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
		// double precision = SPARQLEvaluation.precision(sparqlQuery,
		// targetSPARQLQuery, endpoint);
		// double recall = SPARQLEvaluation.recall(sparqlQuery,
		// targetSPARQLQuery, endpoint);
		// double fMeasure = SPARQLEvaluation.fMeasure(sparqlQuery,
		// targetSPARQLQuery, endpoint);
		// System.out.println("P=" + precision + "\nR=" + recall + "\nF=" +
		// fMeasure);
		//
		// //SELECT COUNT(?x)...
		// sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " +
		// "PREFIX res: <http://dbpedia.org/resource/> " +
		// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		// "SELECT COUNT(DISTINCT ?uri) WHERE {	" +
		// "?uri rdf:type dbo:Film ." +
		// "?uri dbo:starring res:Julia_Roberts .}";
		// targetSPARQLQuery =
		// "PREFIX dbo: <http://dbpedia.org/ontology/> " +
		// "PREFIX res: <http://dbpedia.org/resource/> " +
		// "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
		// "SELECT COUNT(DISTINCT ?uri) WHERE {	" +
		// "?uri rdf:type dbo:Film ." +
		// "?uri dbo:starring res:Julia_Roberts ." +
		// "?uri dbo:director res:Garry_Marshall .}";
		// precision = SPARQLEvaluation.precision(sparqlQuery,
		// targetSPARQLQuery, endpoint);
		// recall = SPARQLEvaluation.recall(sparqlQuery, targetSPARQLQuery,
		// endpoint);
		// fMeasure = SPARQLEvaluation.fMeasure(sparqlQuery, targetSPARQLQuery,
		// endpoint);
		// System.out.println("P=" + precision + "\nR=" + recall + "\nF=" +
		// fMeasure);

	}
}
