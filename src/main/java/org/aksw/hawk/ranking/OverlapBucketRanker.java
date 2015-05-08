package org.aksw.hawk.ranking;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class OverlapBucketRanker {
	static Logger log = LoggerFactory.getLogger(OverlapBucketRanker.class);

	public OverlapBucketRanker() {
	}

	public static void main(String args[]) {
		// TODO transform this to unit test
		List<SPARQLQuery> queries = Lists.newArrayList();

		SPARQLQuery query = new SPARQLQuery("?const <http://dbpedia.org/ontology/starring> ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "Coquette Productions");
		queries.add(query);

		query = new SPARQLQuery("?const ?verb ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "Coquette Productions");
		query.addConstraint("?proj <http://dbpedia.org/ontology/birthPlace> ?const");
		queries.add(query);

		OverlapBucketRanker ranker = new OverlapBucketRanker();
		queries = ranker.rank(queries, 1);
		for (SPARQLQuery q : queries) {
			log.debug(q.toString());
		}
	}

	public List<SPARQLQuery> rank(List<SPARQLQuery> queries, int numberOfReturnQueries) {
		// TODO bug: differentiate between fuzzy and exact matches
		for (SPARQLQuery s : queries) {
			Map<String, Double> calculateRanking = calculateRanking(s);
			double distance = cosinus(calculateRanking, vec);
			s.setScore(distance);
		}
		List<SPARQLQuery> list = Lists.newArrayList(queries);
		Collections.sort(list);
		Collections.reverse(list);
		return list.subList(0, Math.min(numberOfReturnQueries, queries.size()));
	}
}
