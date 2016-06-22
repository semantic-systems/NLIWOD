package org.aksw.autosparql.commons.qald;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

//TODO replace by qa-commons
public class EvaluationUtils {

	public static double precision(final String sparqlQueryString, final String targetSPARQLQueryString, final String endpoint) {
		Query sparqlQuery = QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ);
		sparqlQuery.setDistinct(true);
		Query targetSPARQLQuery = QueryFactory.create(targetSPARQLQueryString, Syntax.syntaxARQ);

		double precision = 0;
		if (sparqlQuery.isSelectType() && targetSPARQLQuery.isSelectType()) {
			Set<RDFNode> nodes = executeSelect(sparqlQuery, endpoint);
			Set<RDFNode> targetNodes = executeSelect(targetSPARQLQuery, endpoint);
			SetView<RDFNode> intersection = Sets.intersection(nodes, targetNodes);
			if (nodes.size() != 0) {
				precision = (double) intersection.size() / (double) nodes.size();
			}
		} else if (sparqlQuery.isAskType() && targetSPARQLQuery.isAskType()) {
			boolean answer = executeAsk(sparqlQuery, endpoint);
			boolean targetAnswer = executeAsk(targetSPARQLQuery, endpoint);
			if (answer == targetAnswer) {
				precision = 1;
			}
		} else {
		}
		return precision;
	}

	public static double recall(final String sparqlQueryString, final String targetSPARQLQueryString, final String endpoint) {
		Query sparqlQuery = QueryFactory.create(sparqlQueryString, Syntax.syntaxARQ);
		sparqlQuery.setDistinct(true);
		Query targetSPARQLQuery = QueryFactory.create(targetSPARQLQueryString, Syntax.syntaxARQ);

		double recall = 0;
		if (sparqlQuery.isSelectType() && targetSPARQLQuery.isSelectType()) {
			// if queries contain aggregation return always 1
			if (!sparqlQuery.getAggregators().isEmpty() && !targetSPARQLQuery.getAggregators().isEmpty()) {
				return 1;
			}
			Set<RDFNode> nodes = executeSelect(sparqlQuery, endpoint);
			Set<RDFNode> targetNodes = executeSelect(targetSPARQLQuery, endpoint);
			SetView<RDFNode> intersection = Sets.intersection(nodes, targetNodes);
			if (nodes.size() != 0) {
				recall = (double) intersection.size() / (double) targetNodes.size();
			}
		} else if (sparqlQuery.isAskType() && targetSPARQLQuery.isAskType()) {
			// if queries are AKS queries return recall=1
			recall = 1;
		} else {
		}
		return recall;
	}

	public static double fMeasure(final String sparqlQuery, final String targetSPARQLQuery, final String endpoint) {
		double precision = precision(sparqlQuery, targetSPARQLQuery, endpoint);
		double recall = recall(sparqlQuery, targetSPARQLQuery, endpoint);
		double fMeasure = 0;
		if (precision + recall > 0) {
			fMeasure = 2 * precision * recall / (precision + recall);
		}
		return fMeasure;
	}

	/**
	 * Returns a set of RDFNode objects by taking the fist projection variable
	 * of the query.
	 * 
	 */
	public static Set<RDFNode> executeSelect(final Query query, final String endpoint) {
		if (query.isQueryResultStar()) {
			// don't know how to handle this in general
			return null;
		}
		ResultSet rs;
		QueryEngineHTTP qe = new QueryEngineHTTP(endpoint, query);
		List<String> defaultGraph = new ArrayList<>();
		defaultGraph.add("http://dbpedia.org");
		qe.setDefaultGraphURIs(defaultGraph);
		rs = qe.execSelect();

		List<Var> projectVars = query.getProjectVars();
		String projectionVar;
		if (!projectVars.isEmpty() && query.getAggregators().isEmpty()) {
			// if there is some projection variable
			projectionVar = query.getProjectVars().get(0).getName();
		} else {// if there is a aggregation, e.g. 'SELECT COUNT(?uri)...'
			projectionVar = rs.getResultVars().get(0);
		}
		Set<RDFNode> nodes = new HashSet<>();
		QuerySolution qs;
		while (rs.hasNext()) {
			qs = rs.next();
			RDFNode rdfNode = qs.get(projectionVar);
			nodes.add(rdfNode);
		}

		return nodes;
	}

	public static boolean executeAsk(final Query query, final String endpoint) {
		QueryEngineHTTP qe = new QueryEngineHTTP(endpoint, query);
		List<String> defaultGraph = new ArrayList<>();
		defaultGraph.add("http://dbpedia.org");
		qe.setDefaultGraphURIs(defaultGraph);
		boolean ret = qe.execAsk();
		return ret;
	}
//TODO christian unit test
	public static void main(final String[] args) throws Exception {
		String sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts .}";
		String targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT DISTINCT ?uri WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts ." + "?uri dbo:director res:Garry_Marshall .}";
		String endpoint = "http://dbpedia.org/sparql";
		double precision = EvaluationUtils.precision(sparqlQuery, targetSPARQLQuery, endpoint);
		double recall = EvaluationUtils.recall(sparqlQuery, targetSPARQLQuery, endpoint);
		double fMeasure = EvaluationUtils.fMeasure(sparqlQuery, targetSPARQLQuery, endpoint);
		System.out.println("P=" + precision + "\nR=" + recall + "\nF=" + fMeasure);

		// SELECT COUNT(?x)...
		sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT COUNT(DISTINCT ?uri) WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts .}";
		targetSPARQLQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> " + "PREFIX res: <http://dbpedia.org/resource/> " + "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
		        + "SELECT COUNT(DISTINCT ?uri) WHERE {	" + "?uri rdf:type dbo:Film ." + "?uri dbo:starring res:Julia_Roberts ." + "?uri dbo:director res:Garry_Marshall .}";
		precision = EvaluationUtils.precision(sparqlQuery, targetSPARQLQuery, endpoint);
		recall = EvaluationUtils.recall(sparqlQuery, targetSPARQLQuery, endpoint);
		fMeasure = EvaluationUtils.fMeasure(sparqlQuery, targetSPARQLQuery, endpoint);
		System.out.println("P=" + precision + "\nR=" + recall + "\nF=" + fMeasure);
	}

}
