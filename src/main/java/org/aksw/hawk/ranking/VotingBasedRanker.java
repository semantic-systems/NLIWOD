package org.aksw.hawk.ranking;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class VotingBasedRanker {
	static Logger log = LoggerFactory.getLogger(VotingBasedRanker.class);
	private RankingDB db;
	private Map<String, Double> vec;

	public VotingBasedRanker() {
		this.db = new RankingDB();
	}

	public static void main(String args[]) {
		// TODO transform this to unit test
		Set<SPARQLQuery> queries = Sets.newHashSet();

		SPARQLQuery query = new SPARQLQuery("?const <http://dbpedia.org/ontology/starring> ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "Coquette Productions");
		queries.add(query);

		query = new SPARQLQuery("?const ?verb ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "Coquette Productions");
		query.addConstraint("?proj <http://dbpedia.org/ontology/birthPlace> ?const");
		queries.add(query);

		VotingBasedRanker ranker = new VotingBasedRanker();
		ranker.train();
		queries = ranker.rank(queries, 1);
		for (SPARQLQuery q : queries) {
			log.debug(q.toString());
		}
	}

	public void train() {
		// TODO remove all sets including those in maps to reduce failure source
		// due to unsure ordering
		// read from file all available pairs
		Set<SPARQLQuery> queries = db.readRankings();
		// build feature vector by averaging
		this.vec = Maps.newHashMap();

		for (SPARQLQuery q : queries) {
			// calculate the ranking for a query given all available gold
			// queries
			Map<String, Double> tmp = calculateRanking(q);
			for (String key : tmp.keySet()) {
				if (vec.containsKey(key)) {
					vec.put(key, tmp.get(key) + vec.get(key));
				} else {
					vec.put(key, tmp.get(key));
				}
			}
		}

		for (String key : vec.keySet()) {
			vec.put(key, vec.get(key) / queries.size());
		}

	}

	private Map<String, Double> calculateRanking(SPARQLQuery q) {
		// here are the features
		Map<String, Double> features = Maps.newHashMap();
		features.putAll(usedPredicates(q));
		features.put("feature:numberOfConstraints", numberOfConstraints(q));
		features.put("feature:numberOfTypes", numberOfTypes(q));

		return features;
	}

	private Map<String, Double> usedPredicates(SPARQLQuery q) {
		// build list of all predicates from gold queries
		Map<String, Double> map = Maps.newHashMap();
		String[] split = new String[3];
		for (String triple : q.constraintTriples) {
			split = triple.split(" ");
			if (map.containsKey(split[1])) {
				double tmp = map.get(split[1]);
				map.put(split[1], tmp + 1);
			} else {
				map.put(split[1], 1.0);
			}
		}
		// TODO talk to Axel about strange normalisation here
		double sum = 0;
		for (String predicateKey : map.keySet()) {
			sum += map.get(predicateKey);
		}
		for (String predicateKey : map.keySet()) {
			double count = map.get(predicateKey);
			map.put(predicateKey, count / sum);
		}
		return map;
	}

	private Double numberOfTypes(SPARQLQuery q) {
		String[] split = new String[3];
		double numberOfTypes = 0;
		for (String triple : q.constraintTriples) {
			split = triple.split(" ");
			if (split[1].equals("a")) {
				numberOfTypes++;
			}
		}
		return numberOfTypes;
	}

	private double numberOfConstraints(SPARQLQuery query) {
		return query.constraintTriples.size();
	}

	public Set<SPARQLQuery> rank(Set<SPARQLQuery> queries, int numberOfReturnQueries) {
		// TODO bug: differentiate between fuzzy and exact matches
		for (SPARQLQuery s : queries) {
			Map<String, Double> calculateRanking = calculateRanking(s);
			double distance = cosinus(calculateRanking, vec);
			s.setScore(distance);
		}
		List<SPARQLQuery> list = Lists.newArrayList(queries);
		Collections.sort(list);
		return Sets.newHashSet(list.subList(0, numberOfReturnQueries));
	}

	private double cosinus(Map<String, Double> calculateRanking, Map<String, Double> goldVector) {
		double dotProduct = 0;
		for (String key : goldVector.keySet()) {
			if (calculateRanking.containsKey(key)) {
				dotProduct += goldVector.get(key) * calculateRanking.get(key);
			}
		}
		double magnitude_A = 0;
		for (String key : goldVector.keySet()) {
			magnitude_A += Math.sqrt(goldVector.get(key) * goldVector.get(key));
		}
		double magnitude_B = 0;
		for (String key : calculateRanking.keySet()) {
			magnitude_B += Math.sqrt(calculateRanking.get(key) * calculateRanking.get(key));
		}

		return dotProduct / (magnitude_A * magnitude_B);
	}

	public void learn(Question q, SPARQLQuery query) {
		db.store(q, query);
	}
}
