package org.aksw.hawk.ranking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class FeatureBasedRanker implements Ranking {
	public enum Feature {
		PREDICATES,
		PATTERN,
		NR_OF_CONSTRAINTS,
		NR_OF_TYPES,
		NR_OF_TERMS
	}

	private static Logger log = LoggerFactory.getLogger(FeatureBasedRanker.class);
	private FeatureBasedRankerDB db = new FeatureBasedRankerDB();
	private Map<String, Double> vec;
	private Collection<Feature> features;

	public void learn(final IQuestion q, final Set<SPARQLQuery> queries) {
		db.store(q, queries);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Answer> rank(final List<Answer> answers, final HAWKQuestion q) {
		Map<Answer, Double> buckets = Maps.newHashMap();

		for (Answer answer : answers) {
			Map<String, Double> calculateRanking = calculateRanking(answer.query);
			double distance = cosinus(calculateRanking, vec);
			answer.score = distance;
			buckets.put(answer, answer.score);
		}

		// sort according to entries in buckets
		List tmplist = new LinkedList(buckets.entrySet());

		Collections.sort(tmplist, new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		List list = new ArrayList<Set<RDFNode>>();
		for (Iterator it = tmplist.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			list.add(entry.getKey());
		}

		return list;
	}

	/**
	 * @param features the features to set
	 */
	public void setFeatures(final Collection<Feature> features) {
		this.features = features;
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
				System.out.println(key);
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

	private void addOneToMapAtKey(final Map<String, Double> map, final String key) {
		if (map.containsKey(key)) {
			map.put(key, map.get(key) + 1.0);
		} else {
			map.put(key, 1.0);
		}
	}

	private Map<String, Double> calculateRanking(final SPARQLQuery q) {
		// a priori assumption
		Collections.sort(q.constraintTriples);
		// here are the features
		Map<String, Double> featureValues = Maps.newHashMap();
		System.out.println("evaluating: " + q.toString());
		for (Feature feature : features) {
			System.out.println("feature:");
			System.out.println(feature);
			switch (feature) {
			case PREDICATES:
				featureValues.putAll(usedPredicates(q));
				break;
			case PATTERN:
				featureValues.putAll(usedPattern(q));
				break;
			case NR_OF_CONSTRAINTS:
				featureValues.put("feature:numberOfConstraints", numberOfConstraints(q));
				break;
			case NR_OF_TERMS:
				featureValues.put("feature:numberOfTermsInTextQuery", numberOfTermsInTextQuery(q));
				break;
			case NR_OF_TYPES:
				featureValues.put("feature:numberOfTypes", numberOfTypes(q));
				break;
			default:
				break;
			}
		}

		return featureValues;
	}

	private double cosinus(final Map<String, Double> calculateRanking, final Map<String, Double> goldVector) {
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

	private double numberOfConstraints(final SPARQLQuery query) {
		return query.constraintTriples.size();
	}

	private Double numberOfTermsInTextQuery(final SPARQLQuery q) {
		// assuming there is only one variable left to search the text
		for (String key : q.textMapFromVariableToSingleFuzzyToken.keySet()) {
			return (double) q.textMapFromVariableToSingleFuzzyToken.get(key).size();
		}
		return 0.0;
	}

	private Double numberOfTypes(final SPARQLQuery q) {
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

	private Map<String, Double> usedPattern(final SPARQLQuery q) {
		// build list of patterns, indicate text position
		Map<String, Double> map = Maps.newHashMap();
		String[] split = new String[3];
		// maybe many bugs down here
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
			String object = split[2];
			if (subject.equals(textNode) && predicate.startsWith("?") && object.startsWith("?")) {
				String key = "textNode_?var_?var";
				addOneToMapAtKey(map, key);
			} else if (subject.equals(textNode) && !predicate.startsWith("?") && object.startsWith("?")) {
				String key = "textNode_bound_?var";
				addOneToMapAtKey(map, key);
			} else if (subject.equals(textNode) && predicate.startsWith("?") && !object.startsWith("?")) {
				String key = "textNode_?var_bound";
				addOneToMapAtKey(map, key);
			} else if (subject.equals(textNode) && !predicate.startsWith("?") && !object.startsWith("?")) {
				String key = "textNode_bound_bound";
				addOneToMapAtKey(map, key);
			} else if (object.equals(textNode) && predicate.startsWith("?") && subject.startsWith("?")) {
				String key = "?var_?var_textNode";
				addOneToMapAtKey(map, key);
			} else if (object.equals(textNode) && !predicate.startsWith("?") && subject.startsWith("?")) {
				String key = "?var_bound_textNode";
				addOneToMapAtKey(map, key);
			} else if (object.equals(textNode) && predicate.startsWith("?") && !subject.startsWith("?")) {
				String key = "bound_?var_textNode";
				addOneToMapAtKey(map, key);
			} else if (object.equals(textNode) && !predicate.startsWith("?") && !subject.startsWith("?")) {
				String key = "bound_bound_textNode";
				addOneToMapAtKey(map, key);
			}

		}

		return map;
	}

	private Map<String, Double> usedPredicates(final SPARQLQuery q) {
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

}
