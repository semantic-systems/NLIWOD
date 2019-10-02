package org.aksw.qa.commons.measure;

import java.util.Set;

import org.aksw.qa.commons.sparql.SPARQL;
import org.aksw.qa.commons.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses the dbpedia.org/sparql endpoint to check two given sparql
 * queries whether they return the same result and calculate precision, recall
 * and f-measure out of it
 * 
 * @author ricardousbeck
 *
 */
public class SPARQLBasedEvaluation {

	private static Logger logger = LoggerFactory.getLogger(SPARQLBasedEvaluation.class);

	public static double precision(String sparqlQueryString, String targetSPARQLQueryString, String endpoint) {
		// Query sparqlQuery = QueryFactory.create(sparqlQueryString,
		// Syntax.syntaxARQ);
		// sparqlQuery.setDistinct(true);
		// Query targetSPARQLQuery =
		// QueryFactory.create(targetSPARQLQueryString, Syntax.syntaxARQ);

		double precision = 0;
		if (isSelectType(sparqlQueryString) && isSelectType(targetSPARQLQueryString)) {
			Set<String> nodes = SPARQL.executeSelect(sparqlQueryString, endpoint).getStringSet();
			Set<String> targetNodes = SPARQL.executeSelect(targetSPARQLQueryString, endpoint).getStringSet();
			Set<String> intersection = CollectionUtils.intersection(nodes, targetNodes);
			if (nodes.size() != 0) {
				precision = (double) intersection.size() / (double) nodes.size();
			}
		} else if (isAskType(sparqlQueryString) && isAskType(targetSPARQLQueryString)) {
			boolean answer = SPARQL.executeAsk(sparqlQueryString, endpoint);
			boolean targetAnswer = SPARQL.executeAsk(targetSPARQLQueryString, endpoint);
			if (answer == targetAnswer) {
				precision = 1;
			}
		} else {
			logger.error("Not implemented SPARQL query type.");
		}
		return precision;
	}

	public static double recall(String sparqlQueryString, String targetSPARQLQueryString, String endpoint) {
		// Query sparqlQuery = QueryFactory.create(sparqlQueryString,
		// Syntax.syntaxARQ);
		// sparqlQuery.setDistinct(true);
		// Query targetSPARQLQuery =
		// QueryFactory.create(targetSPARQLQueryString, Syntax.syntaxARQ);

		double recall = 0;
		if (isSelectType(sparqlQueryString) && isSelectType(targetSPARQLQueryString)) {
			// if queries contain aggregation return always 1
			if (hasAggregations(sparqlQueryString) && hasAggregations(targetSPARQLQueryString)) {
				return 1;
			}
			Set<String> nodes = SPARQL.executeSelect(sparqlQueryString, endpoint).getStringSet();
			Set<String> targetNodes = SPARQL.executeSelect(targetSPARQLQueryString, endpoint).getStringSet();
			Set<String> intersection = CollectionUtils.intersection(nodes, targetNodes);
			if (nodes.size() != 0) {
				recall = (double) intersection.size() / (double) targetNodes.size();
			}
		} else if (isAskType(sparqlQueryString) && isAskType(targetSPARQLQueryString)) {
			// if queries are AKS queries return recall=1
			recall = 1;
		} else {
			logger.error("Not implemented SPARQL query type.");
		}
		return recall;
	}

	public static double fMeasure(String sparqlQuery, String targetSPARQLQuery, String endpoint) {
		double precision = precision(sparqlQuery, targetSPARQLQuery, endpoint);
		double recall = recall(sparqlQuery, targetSPARQLQuery, endpoint);
		double fMeasure = 0;
		if (precision + recall > 0) {
			fMeasure = 2 * precision * recall / (precision + recall);
		}
		return fMeasure;
	}

	private static boolean isAskType(String sparqlQuery) {
		return sparqlQuery.contains("\nASK\n") || sparqlQuery.contains("ASK ");
	}

	private static boolean isSelectType(String sparqlQuery) {
		return sparqlQuery.contains("\nSELECT\n") || sparqlQuery.contains("SELECT ");
	}

	private static boolean hasAggregations(String query) {
		return query.toLowerCase().contains("count") || query.toLowerCase().contains("sum");
	}

}
