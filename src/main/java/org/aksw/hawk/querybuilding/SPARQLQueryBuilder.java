package org.aksw.hawk.querybuilding;

import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.pruner.SPARQLQueryPruner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SPARQLQueryBuilder {
	int numberOfOverallQueriesExecuted = 0;
	private static Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder.class);
	private SPARQL sparql;
	private RecursiveSparqlQueryBuilder recursiveSparqlQueryBuilder;
	private SPARQLQueryPruner sparqlQueryPruner;

	public SPARQLQueryBuilder(SPARQL sparql) {
		this.sparql = sparql;
		this.recursiveSparqlQueryBuilder = new RecursiveSparqlQueryBuilder();
		this.sparqlQueryPruner = new SPARQLQueryPruner(sparql);
	}

	public Map<String, Set<RDFNode>> build(Question q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();
		try {
			// build sparql queries
			Set<SPARQLQuery> queryStrings = recursiveSparqlQueryBuilder.start(this, q);

			// pruning
			queryStrings = sparqlQueryPruner.prune(queryStrings);
			
			// transforming to SPARQL
			int i = 0;
			for (SPARQLQuery query : queryStrings) {
				for (String queryString : query.generateQueries()) {
					log.debug(i++ + "/" + queryStrings.size() * query.generateQueries().size() + "= " + queryString);
					Set<RDFNode> answerSet = sparql.sparql(queryString);
					if (!answerSet.isEmpty()) {
						answer.put(queryString, answerSet);
					}
					numberOfOverallQueriesExecuted++;
				}
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		} finally {
			System.gc();
		}
		log.debug("Number of sofar executed queries: " + numberOfOverallQueriesExecuted);
		return answer;
	}
}