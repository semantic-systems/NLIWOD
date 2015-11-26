package org.aksw.qa.commons.measure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLEvaluationTest {
	String endpoint = "http://dbpedia.org/sparql";
	Logger logger = LoggerFactory.getLogger(SPARQLEvaluationTest.class);

	@Test
	public void testEndpointAvailibility() {
		// Query should be validate for every Endpoint
		String queryString = "SELECT * WHERE {?s ?p ?o} LIMIT 1";
		// Validates Query String itself
		Query query = QueryFactory.create(queryString);
		QueryEngineHTTP qe = null;
		try {
			qe = new QueryEngineHTTP(endpoint, query);
			ResultSet res = qe.execSelect();
			// Is ResultSet == null Endpoint is with a high probability not
			// available
			assertNotNull(res);
		} catch (Exception e) {
			logger.debug("Endpoint is not available due to: ", e);
			assertNotNull(null);
		} finally {
			// close QueryEngine
			if (qe != null) {
				qe.close();
			}
		}
	}

	@Test
	public void testTooGenericQuery() {
		String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ "PREFIX res: <http://dbpedia.org/resource/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ."
				+ "?uri dbo:starring res:Julia_Roberts .}";
		String targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ "PREFIX res: <http://dbpedia.org/resource/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT DISTINCT ?uri WHERE {	"
				+ "?uri rdf:type dbo:Film ."
				+ "?uri dbo:starring res:Julia_Roberts ."
				+ "?uri dbo:director res:Garry_Marshall .}";
		double precision = SPARQLEvaluation.precision(sparqlQuery,
				targetSPARQLQuery, endpoint);
		double recall = SPARQLEvaluation.recall(sparqlQuery, targetSPARQLQuery,
				endpoint);
		double fMeasure = SPARQLEvaluation.fMeasure(sparqlQuery,
				targetSPARQLQuery, endpoint);
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
		String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ "PREFIX res: <http://dbpedia.org/resource/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT COUNT(DISTINCT ?uri) WHERE {	"
				+ "?uri rdf:type dbo:Film ."
				+ "?uri dbo:starring res:Julia_Roberts .}";
		String targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> "
				+ "PREFIX res: <http://dbpedia.org/resource/> "
				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
				+ "SELECT COUNT(DISTINCT ?uri) WHERE {	"
				+ "?uri rdf:type dbo:Film ."
				+ "?uri dbo:starring res:Julia_Roberts ."
				+ "?uri dbo:director res:Garry_Marshall .}";
		double precision = SPARQLEvaluation.precision(sparqlQuery,
				targetSPARQLQuery, endpoint);
		double recall = SPARQLEvaluation.recall(sparqlQuery, targetSPARQLQuery,
				endpoint);
		double fMeasure = SPARQLEvaluation.fMeasure(sparqlQuery,
				targetSPARQLQuery, endpoint);

		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		logger.debug("P=" + precision);
		logger.debug("R=" + recall);
		logger.debug("F=" + fMeasure);
	}

	// TODO also transform to unit test
//	@Test
//	public void testWhatEver() {
//		String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> "
//				+ "PREFIX res: <http://dbpedia.org/resource/> "
//				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//				+ "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ."
//				+ "?uri dbo:starring res:Julia_Roberts .}";
//		String targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> "
//				+ "PREFIX res: <http://dbpedia.org/resource/> "
//				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//				+ "SELECT DISTINCT ?uri WHERE {	"
//				+ "?uri rdf:type dbo:Film ."
//				+ "?uri dbo:starring res:Julia_Roberts ."
//				+ "?uri dbo:director res:Garry_Marshall .}";
//		double precision = SPARQLEvaluation.precision(sparqlQuery,
//				targetSPARQLQuery, endpoint);
//		double recall = SPARQLEvaluation.recall(sparqlQuery, targetSPARQLQuery,
//				endpoint);
//		double fMeasure = SPARQLEvaluation.fMeasure(sparqlQuery,
//				targetSPARQLQuery, endpoint);
//		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
//		logger.debug("P=" + precision);
//		logger.debug("R=" + recall);
//		logger.debug("F=" + fMeasure);
//
//		// SELECT COUNT(?x)...
//		sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> "
//				+ "PREFIX res: <http://dbpedia.org/resource/> "
//				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//				+ "SELECT COUNT(DISTINCT ?uri) WHERE {	"
//				+ "?uri rdf:type dbo:Film ."
//				+ "?uri dbo:starring res:Julia_Roberts .}";
//		targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> "
//				+ "PREFIX res: <http://dbpedia.org/resource/> "
//				+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
//				+ "SELECT COUNT(DISTINCT ?uri) WHERE {	"
//				+ "?uri rdf:type dbo:Film ."
//				+ "?uri dbo:starring res:Julia_Roberts ."
//				+ "?uri dbo:director res:Garry_Marshall .}";
//		precision = SPARQLEvaluation.precision(sparqlQuery, targetSPARQLQuery,
//				endpoint);
//		recall = SPARQLEvaluation.recall(sparqlQuery, targetSPARQLQuery,
//				endpoint);
//		fMeasure = SPARQLEvaluation.fMeasure(sparqlQuery, targetSPARQLQuery,
//				endpoint);
//		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
//		logger.debug("P=" + precision);
//		logger.debug("R=" + recall);
//		logger.debug("F=" + fMeasure);
//
//	}
}
