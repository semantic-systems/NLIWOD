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

public class VotingBasedRanker {
	static Logger log = LoggerFactory.getLogger(VotingBasedRanker.class);
	private RankingDB db;
	private Map<String, Double> vec;

	public VotingBasedRanker() {
		this.db = new RankingDB();
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
			log.debug(key + ": " + vec.get(key));
		}

	}

	private Map<String, Double> calculateRanking(SPARQLQuery q) {
		// a priori assumption
		Collections.sort(q.constraintTriples);

		// here are the features
		Map<String, Double> features = Maps.newHashMap();
		features.putAll(usedPredicates(q));
		features.putAll(usedPattern(q));
		features.put("feature:numberOfTermsInTextQuery", numberOfTermsInTextQuery(q));
		// features.put("feature:numberOfConstraints", numberOfConstraints(q));
		// features.put("feature:numberOfTypes", numberOfTypes(q));

		return features;
	}

	private Map<String, Double> usedPattern(SPARQLQuery q) {
		// build list of patterns, indicate text position
		Map<String, Double> map = Maps.newHashMap();
		String[] split = new String[3];
		// TODO maybe many bugs down here
		// http://mathinsight.org/media/image/image/three_node_motifs.png
		// 1) find out the text node
		String textNode = null;
		for (String var : q.textMapFromVariableToCombinedNNExactMatchToken.keySet()) {
			textNode = var;
		}

		// 2) measure for all one edge motifs (without the text:query edge)
		// measure all 16 motifs, take always text node as central node
		List<String> constraintTriples = q.constraintTriples;
		for (String triple : constraintTriples) {
			triple = triple.replaceAll("\\s+", " ");
			split = triple.split(" ");
			String subject = split[0];
			String predicate = split[1];
			if (subject.equals(textNode) && predicate.startsWith("?") && object(split).startsWith("?")) {
				String key = "textNode_?var_?var";
				addOneToMapAtKey(map, key);
			} else if (subject.equals(textNode) && !predicate.startsWith("?") && object(split).startsWith("?")) {
				String key = "textNode_bound_?var";
				addOneToMapAtKey(map, key);
			} else if (subject.equals(textNode) && predicate.startsWith("?") && !object(split).startsWith("?")) {
				String key = "textNode_?var_bound";
				addOneToMapAtKey(map, key);
			} else if (subject.equals(textNode) && !predicate.startsWith("?") && !object(split).startsWith("?")) {
				String key = "textNode_bound_bound";
				addOneToMapAtKey(map, key);
			} else if (object(split).equals(textNode) && predicate.startsWith("?") && subject.startsWith("?")) {
				String key = "?var_?var_textNode";
				addOneToMapAtKey(map, key);
			} else if (object(split).equals(textNode) && !predicate.startsWith("?") && subject.startsWith("?")) {
				String key = "?var_bound_textNode";
				addOneToMapAtKey(map, key);
			} else if (object(split).equals(textNode) && predicate.startsWith("?") && !subject.startsWith("?")) {
				String key = "bound_?var_textNode";
				addOneToMapAtKey(map, key);
			} else if (object(split).equals(textNode) && !predicate.startsWith("?") && !subject.startsWith("?")) {
				String key = "bound_bound_textNode";
				addOneToMapAtKey(map, key);
			}

		}

		return map;
	}

	private String object(String[] split) {
		return split[2];
	}

	private void addOneToMapAtKey(Map<String, Double> map, String key) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + 1.0);
		} else {
			map.put(key, 1.0);
		}
	}

	private Double numberOfTermsInTextQuery(SPARQLQuery q) {
		// assuming there is only one variable left to search the text
		for (String key : q.textMapFromVariableToSingleFuzzyToken.keySet()) {
			return (double) q.textMapFromVariableToSingleFuzzyToken.get(key).size();
		}
		return 0.0;
	}

	private Map<String, Double> usedPredicates(SPARQLQuery q) {
		// build list of all predicates from gold queries
		Map<String, Double> map = Maps.newHashMap();
		String[] split = new String[3];
		for (String triple : q.constraintTriples) {
			triple = triple.replaceAll("\\s+", " ");
			split = triple.split(" ");
			if (map.containsKey(split[1])) {
				double tmp = map.get(split[1]);
				map.put(split[1], tmp + 1);
			} else {
				map.put(split[1], 1.0);
			}
		}
		// TODO talk to Axel about strange normalisation here
		// double sum = 0;
		// for (String predicateKey : map.keySet()) {
		// sum += map.get(predicateKey);
		// }
		// for (String predicateKey : map.keySet()) {
		// double count = map.get(predicateKey);
		// map.put(predicateKey, count / sum);
		// }
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

	public void learn(Question q, Set<SPARQLQuery> queries) {
		db.store(q, queries);
	}
}
