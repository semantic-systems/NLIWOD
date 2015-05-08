package org.aksw.hawk.ranking;

import java.util.Map;
import java.util.Set;

import org.aksw.hawk.controller.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class OverlapBucketRanker {
	static Logger log = LoggerFactory.getLogger(OverlapBucketRanker.class);

	public Map<String, Answer> rank(Map<String, Answer> answers, int numberOfReturnQueries) {

		Map<Set<RDFNode>, Integer> buckets = Maps.newHashMap();

		for (String keyQuery : answers.keySet()) {
			Answer set = answers.get(keyQuery);

			if (buckets.containsKey(set.answerSet)) {
				int count = buckets.get(set.answerSet) + 1;
				buckets.put(set.answerSet, count);
			} else {
				buckets.put(set.answerSet, 1);
			}
		}

		Set<RDFNode> maxSet = null;
		int maxCount = 0;
		for (Set<RDFNode> set : buckets.keySet()) {
			log.debug("set: " + set + "\t->" + buckets.get(set));
			if (buckets.get(set) > maxCount) {
				maxCount = buckets.get(set);
				maxSet = set;
			}
		}

		for (String keyQuery : answers.keySet()) {
			Answer answer = answers.get(keyQuery);
			if (answer.answerSet.equals(maxSet)) {
				Map<String, Answer> returnMap = Maps.newHashMap();
				returnMap.put(keyQuery, answer);
				return returnMap;
			}

		}
		return Maps.newHashMap();
	}
}
