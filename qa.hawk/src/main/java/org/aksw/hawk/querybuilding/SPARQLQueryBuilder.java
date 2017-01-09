package org.aksw.hawk.querybuilding;

import java.util.List;
import java.util.Set;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.pruner.SPARQLQueryPruner;
import org.aksw.qa.commons.sparql.SPARQL;
import org.aksw.qa.commons.sparql.SPARQLQuery;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

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

	@SuppressWarnings("unchecked")
	public List<Answer> build(HAWKQuestion q) {
		// FIXME refactor that to pipeline steps
		List<Answer> answer = Lists.newArrayList();
		try {
			// build sparql queries
			Set<SPARQLQuery> queryStrings = recursiveSparqlQueryBuilder.start(this, q);

			// pruning
			queryStrings = sparqlQueryPruner.prune(queryStrings, q);

			// identify the cardinality of the answers
			int cardinality = cardinality(q, queryStrings);
			JSONObject tmp = new JSONObject();
			tmp.put("label", "Cardinality of question results");
			tmp.put("value", cardinality);
			q.getPruning_messages().add(tmp);
			log.debug("Cardinality:" + q.getLanguageToQuestion().get("en").toString() + "-> " + cardinality);
			int i = 0;
			for (SPARQLQuery query : queryStrings) {
				for (String queryString : query.generateQueries()) {
					log.debug(i++ + "/" + queryStrings.size() * query.generateQueries().size() + "= " + queryString);
					Answer a = new Answer();
					a.answerSet = sparql.sparql(queryString);
					a.query = query;
					a.queryString = queryString;
					a.question_id = q.getId();
					a.question = q.getLanguageToQuestion().get("en").toString();
					if (!a.answerSet.isEmpty()) {
						answer.add(a);
					}
					numberOfOverallQueriesExecuted++;
				}
			}
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		} finally {
			System.gc();
		}
		JSONObject tmp = new JSONObject();
		tmp.put("label", "Number of sofar executed queries");
		tmp.put("value", numberOfOverallQueriesExecuted);
		q.getPruning_messages().add(tmp);
		log.debug("Number of sofar executed queries: " + numberOfOverallQueriesExecuted);
		return answer;
	}

	private int cardinality(HAWKQuestion q, Set<SPARQLQuery> queryStrings) {
		int cardinality = q.getCardinality();
		// find a way to determine the cardinality of the answer

		for (SPARQLQuery s : queryStrings) {
			s.setLimit(cardinality);
		}
		return cardinality;
	}

}