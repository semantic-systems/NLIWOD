package org.aksw.qa.commons.sparql;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.qa.commons.qald.QALD4_EvaluationUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Executes sparql queries.
 */
public class SPARQL {
	private Logger log = LoggerFactory.getLogger(SPARQL.class);
	public QueryExecutionFactory qef;

	private long timeToLive = 360l * 24l * 60l * 60l * 1000l;

	/**
	 * {@link SPARQLEndpoints#DBPEDIA_ORG} as endpoint used.
	 */
	public SPARQL() {
		try {

			CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend("./sparql", true, timeToLive);

			// AKSW SPARQL API call
			qef = FluentQueryExecutionFactory.http(SPARQLEndpoints.DBPEDIA_ORG).config().withCache(cacheFrontend).end().create();

		} catch (RuntimeException e) {
			log.error("Could not create SPARQL interface! ", e);
		}
	}

	/**
	 * @param endpoint
	 *            - A sparql endpoint, e.g. {@link SPARQLEndpoints#DBPEDIA_ORG}
	 */
	public SPARQL(final String endpoint) {
		try {
			CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend("./sparql", true, timeToLive);
			qef = FluentQueryExecutionFactory.http(endpoint).config().withCache(cacheFrontend).end().create();
		} catch (RuntimeException e) {
			log.error("Could not create SPARQL interface! ", e);
		}
	}

	/**
	 * Fire a sparql query against endpoint defined in constructor.
	 * <p>
	 * <b>NOTE:</b> This will block. To set a maximum execution time, use {@link ThreadedSPARQL}
	 * <p>
	 * For string representation of answers, see {@link #extractAnswerStrings(Set)}
	 *
	 * @param query
	 *            - a sparql query
	 * @return
	 * @throws ExecutionException
	 */
	public Set<RDFNode> sparql(final String query) throws ExecutionException {
		Set<RDFNode> set = Sets.newHashSet();

		QueryExecution qe = qef.createQueryExecution(query);
		if ((qe != null) && (query.toString() != null)) {
			if (QALD4_EvaluationUtils.isAskType(query)) {
				set.add(new ResourceImpl(String.valueOf(qe.execAsk())));
			} else {
				ResultSet results = qe.execSelect();
				String firstVarName = results.getResultVars().get(0);
				while (results.hasNext()) {

					RDFNode node = results.next().get(firstVarName);
					/**
					 * Instead of returning a set with size 1 and value (null) in it, when no answers are found, this ensures that Set is empty
					 */
					if (node != null) {
						set.add(node);
					}
				}
			}
			qe.close();
		}

		return set;
	}

	/**
	 * For use with {@link #sparql(String)} Extracts answer strings. Can be directly set as golden answers in IQuesion.
	 *
	 * @param answers
	 * @return
	 */
	public static Set<String> extractAnswerStrings(final Set<RDFNode> answers) {
		Set<String> set = Sets.newHashSet();
		for (RDFNode answ : answers) {
			if (answ.isResource()) {
				set.add(answ.asResource().getURI());
			} else if (answ.isLiteral()) {
				Literal l = (Literal) answ;
				try {
					set.add(l.getString());
				} catch (Exception e) {
					e.printStackTrace();
					set.add(l.getLexicalForm());
				}

			} else {
				set.add(answ.toString());
			}
		}
		return set;
	}

	/**
	 * @return - The time to live of frontendCache
	 */
	public long getCacheTimeToLive() {
		return timeToLive;
	}

	/**
	 * @return - Tet the time to live for frontendCache
	 */
	public void setCacheTimeToLive(final long timeToLive) {
		this.timeToLive = timeToLive;
	}

	/**
	 * Tries to parse query with apache.jena . If fails, returns false.
	 *
	 * @param sparql
	 * @return
	 */
	public static boolean isValidSparqlQuery(final String sparql) {
		try {
			QueryFactory.create(sparql);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// TODO Christian: transform to unit test
	public static void main(final String args[]) throws Exception {
		SPARQL sqb = new SPARQL();
		// TODO @ricardo from jonathan please take a moment to look at this:

		// TODO In order of generateQueries() to work
		// you need to set something to
		// SparqlQuery.textMapFromVariableToCombinedNNExactMatchToken
		// or to SparqlQuery.textMapFromVariableToSingleFuzzyToken, which are
		// public Maps
		// But for a query with a few basic constraints you wouldnt set
		// something there?! So, only by setting Constraints, generateQueries
		// will always be empty
		SPARQLQuery query = new SPARQLQuery();
		query.addConstraint("?proj a <http://dbpedia.org/ontology/Person>.");
		// query.addConstraint("?proj ?p ?const.");
		// query.addFilter("proj",
		// Lists.newArrayList("http://dbpedia.org/resource/Pope_John_Paul_I",
		// "http://dbpedia.org/resource/Pope_John_Paul_II"));
		// query.addFilter("const",
		// Lists.newArrayList("http://dbpedia.org/resource/Canale_d'Agordo"));

		for (String q : query.generateQueries()) {
			Set<RDFNode> set = sqb.sparql(q);
			for (RDFNode item : set) {
				System.out.println(item);
			}
		}
	}
}
