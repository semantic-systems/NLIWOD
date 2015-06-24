package org.aksw.hawk.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * Ranks based on answer set frequency. The most seen answer set<RDFNode> will be returned
 * 
 * @author ricardousbeck
 *
 */
public class BucketRanker implements Ranking {
	static Logger log = LoggerFactory.getLogger(BucketRanker.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List<Set<RDFNode>> rank(List<Answer> answers, Question q) {

		Map<Set<RDFNode>, Integer> buckets = Maps.newHashMap();

		for (Answer answer : answers) {

			if (buckets.containsKey(answer.answerSet)) {
				int count = buckets.get(answer.answerSet) + 1;
				buckets.put(answer.answerSet, count);
			} else {
				buckets.put(answer.answerSet, 1);
			}
		}

		//sort according to entries in buckets
		List tmplist = new LinkedList(buckets.entrySet());

		Collections.sort(tmplist, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});
		
		List list = new ArrayList<Set<RDFNode>>();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			list.add(entry.getValue());
		}

		return list;
	}

}
