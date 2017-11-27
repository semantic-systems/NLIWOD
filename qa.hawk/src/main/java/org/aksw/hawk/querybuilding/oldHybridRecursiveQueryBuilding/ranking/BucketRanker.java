package org.aksw.hawk.querybuilding.oldHybridRecursiveQueryBuilding.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Ranks based on answer set frequency. The most seen answer set RDFNode will
 * be returned
 * 
 * @author ricardousbeck
 *
 */
public class BucketRanker implements Ranking {
	static Logger log = LoggerFactory.getLogger(BucketRanker.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Answer> rank(List<Answer> answers, HAWKQuestion q) {
		// TODO Christian: bug here!?!?, write a unit test for bucketbased
		// ranking
		Map<Answer, Integer> buckets = Maps.newHashMap();

		for (Answer answer : answers) {

			if (buckets.containsKey(answer)) {
				int count = buckets.get(answer) + 1;
				buckets.put(answer, count);
			} else {
				buckets.put(answer, 1);
			}
		}

		// sort according to entries in buckets
		List tmplist = new LinkedList(buckets.entrySet());

		Collections.sort(tmplist, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		Collections.reverse(tmplist);

		List<Answer> list = new ArrayList<Answer>();
		for (Iterator it = tmplist.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Answer answer = (Answer) entry.getKey();
			answer.score = (double) ((Integer) entry.getValue());
			list.add(answer);
		}

		return list;
	}
}
