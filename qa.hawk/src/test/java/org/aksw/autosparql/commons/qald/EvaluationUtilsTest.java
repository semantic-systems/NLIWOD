package org.aksw.autosparql.commons.qald;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import infrastructure.ServerChecks;

public class EvaluationUtilsTest {
	static Logger log = LoggerFactory.getLogger(EvaluationUtilsTest.class);

	@BeforeClass
	public static void checkServer() {
		if (!ServerChecks.titanSparqlAlive()) {
			throw new Error("Server down");
		}
	}

	@Test
	public void test() {
		String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts .}";
		String targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts ." + "?uri dbo:director res:Garry_Marshall .}";
		String endpoint = "http://139.18.2.164:3030/ds/sparql";
		double precision = EvaluationUtils.precision(sparqlQuery, targetSPARQLQuery, endpoint);
		double recall = EvaluationUtils.recall(sparqlQuery, targetSPARQLQuery, endpoint);
		double fMeasure = EvaluationUtils.fMeasure(sparqlQuery, targetSPARQLQuery, endpoint);
		log.debug("Precision: " + precision + " Recall: " + recall + " fMeasure: " + fMeasure);

		Assert.assertTrue(precision == 0.0d);
		Assert.assertTrue(recall == 0.0d);
		Assert.assertTrue(fMeasure == 0.0d);

		// SELECT COUNT(?x)...
		sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT COUNT(DISTINCT ?uri) WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts .}";
		targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT COUNT(DISTINCT ?uri) WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts ." + "?uri dbo:director res:Garry_Marshall .}";
		precision = EvaluationUtils.precision(sparqlQuery, targetSPARQLQuery, endpoint);
		recall = EvaluationUtils.recall(sparqlQuery, targetSPARQLQuery, endpoint);
		fMeasure = EvaluationUtils.fMeasure(sparqlQuery, targetSPARQLQuery, endpoint);
		log.debug("Precision: " + precision + " Recall: " + recall + " fMeasure: " + fMeasure);

		Assert.assertTrue(precision == 0.0d);
		Assert.assertTrue(recall == 1.0d);
		Assert.assertTrue(fMeasure == 0.0d);
	}

}
