package org.aksw.hawk.querybuilding;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.pruner.SPARQLQueryPruner;
import org.aksw.hawk.ranking.VotingBasedRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

	public Map<String, Answer> buildWithRanking(Question q, VotingBasedRanker ranker) {
		Map<String, Answer> answer = Maps.newHashMap();
		try {
			// build sparql queries
			Set<SPARQLQuery> queryStrings = recursiveSparqlQueryBuilder.start(this, q);

			// pruning
			queryStrings = sparqlQueryPruner.prune(queryStrings);

			// identify the cardinality of the answers
			int cardinality = cardinality(q, queryStrings);
			log.debug("Cardinality:" + q.languageToQuestion.get("en").toString() + "-> " + cardinality);
			// TODO refactor the next line with this senseless list creation
			List<SPARQLQuery> rankedQueries = ranker.rank(Lists.newArrayList(queryStrings), Integer.MAX_VALUE);
			// transforming to SPARQL
			int i = 0;
			// TODO this for-loop is a hack because ranking does not always
			// deliver a filled result set stuff
			// thus take the first query generating any result

			// FIXME refactor this and the pipeline class to allow training on
			// the one side and real testing on the other. this influences
			// RAnker, Pipeline and this class

			// measuring f-measure at n
			// for (int x = 0; x < rankedQueries.size() &&
			// answer.keySet().isEmpty(); x++) {
			for (int x = 0; x < rankedQueries.size(); x++) {
				SPARQLQuery query = rankedQueries.get(x);
				for (String queryString : query.generateQueries()) {
					log.debug(i++ + "/" + rankedQueries.size() * query.generateQueries().size() + "= " + queryString);
					Answer a = new Answer();
					a.answerSet = sparql.sparql(queryString);
					a.query = query;
					a.question_id = q.id;
					a.question=q.languageToQuestion.get("en").toString();
					if (!a.answerSet.isEmpty()) {
						answer.put(queryString, a);
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

	public Map<String, Answer> build(Question q) {
		Map<String, Answer> answer = Maps.newHashMap();
		try {
			// build sparql queries
			Set<SPARQLQuery> queryStrings = recursiveSparqlQueryBuilder.start(this, q);

			// pruning
			queryStrings = sparqlQueryPruner.prune(queryStrings);

			// identify the cardinality of the answers
			int cardinality = cardinality(q, queryStrings);
			log.debug("Cardinality:" + q.languageToQuestion.get("en").toString() + "-> " + cardinality);
			int i = 0;
			for (SPARQLQuery query : queryStrings) {
				for (String queryString : query.generateQueries()) {
					log.debug(i++ + "/" + queryStrings.size() * query.generateQueries().size() + "= " + queryString);
					Answer a = new Answer();
					a.answerSet = sparql.sparql(queryString);
					a.query = query;
					a.question_id = q.id;
					a.question=q.languageToQuestion.get("en").toString();
					if (!a.answerSet.isEmpty()) {
						answer.put(queryString, a);
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

	private int cardinality(Question q, Set<SPARQLQuery> queryStrings) {
		int cardinality = q.cardinality;
		// find a way to determine the cardinality of the answer

		for (SPARQLQuery s : queryStrings) {
			s.setLimit(cardinality);
		}
		return cardinality;
	}

}