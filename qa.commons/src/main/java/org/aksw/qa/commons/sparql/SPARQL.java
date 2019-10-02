package org.aksw.qa.commons.sparql;

import java.util.ArrayList;
import java.util.Set;

import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.qa.commons.qald.QALD4_EvaluationUtils;
import org.aksw.qa.commons.utils.Results;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
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
	 */
	public Set<RDFNode> sparql(final String query) {
		Set<RDFNode> set = Sets.newHashSet();

		QueryExecution qe = qef.createQueryExecution(query);
		if ((qe != null) && (query != null)) {
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
	 * For use with {@link #sparql(String)} Extracts answer strings. Can be directly set as golden answers in an IQuestion.
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
	 * Executes a select query for the given endpoint and query. Returns the answer as an {@link Results} object.
	 * @param query
	 * @param endpoint
	 * @return
	 */
	public static Results executeSelect(final String query, final String endpoint) {
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint);
		QueryExecution qe = qef.createQueryExecution(query);

		ResultSet rs = qe.execSelect();	
		
		Results res = new Results();
		res.header.addAll(rs.getResultVars());

		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			res.table.add(new ArrayList<String>());
			for(String head: res.header) {
				String answer = "";
				
				if(sol.get(head).isResource()) {
					answer = sol.getResource(head).toString();
				} else {
					String temp = sol.get(head).toString();
					if(temp.contains("@")) {
						answer = "\"" + temp.substring(0, temp.indexOf("@")) + "\"" + temp.substring(temp.indexOf("@"));
					} else if (temp.contains("^^")){
						answer = "\"" + temp.substring(0, temp.indexOf("^")) + "\"^^<" + temp.substring(temp.indexOf("^")+2) + ">";
					} else {
						answer = temp;
					}
				}
				res.table.get(res.table.size()-1).add(answer);
			}
		}		
		closeExecFactory(qef);
		return res;
	}

	/**
	 * Executes an ask query for the given endpoint and query.
	 * @param query
	 * @param endpoint
	 * @return
	 */
	public static Boolean executeAsk(final String query, final String endpoint) {
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint);
		QueryExecution qe = qef.createQueryExecution(query);		
		closeExecFactory(qef);
		return qe.execAsk();
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
	
	/**
	 * Checks if the given endpoint is alive. If fails, returns false.
	 * @param endpoint
	 * @return
	 */
	public static boolean isEndpointAlive(final String endpoint) {		
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint);
		try {
			QueryExecution qe = qef.createQueryExecution("PREFIX foaf:    <http://xmlns.com/foaf/0.1/> ASK  { ?x foaf:name  \"Alice\" }");
			qe.execAsk();
			return true;
		} catch (Exception e) {
	
		} finally {
			closeExecFactory(qef);
		}
		return false;
	}
	
	private static void closeExecFactory(QueryExecutionFactory qef) {
		if(qef != null) {
			try {
				qef.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}	
	}
}
