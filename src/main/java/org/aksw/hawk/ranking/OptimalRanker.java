package org.aksw.hawk.ranking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.Maps;

/**
 * optimal in a sense of fmeasure
 * 
 * @author ricardousbeck
 *
 */
public class OptimalRanker implements Ranking {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Answer> rank(final List<Answer> answers, final HAWKQuestion q) {
		// Compare to set of resources from benchmark
		// save score for for answerSet with fmeasure > 0
		Map<Answer, Double> buckets = Maps.newHashMap();

		for (Answer answer : answers) {
			Set<RDFNode> answerSet = answer.answerSet;
			// TODO check whether q has a gold standard set
			// should be checked in qa-commons
			double fMeasure = QALD4_EvaluationUtils.fMeasure(answerSet, q);

			if (fMeasure > 0) {
				buckets.put(answer, fMeasure);
			}
		}

		// sort according to entries in buckets
		List tmplist = new LinkedList(buckets.entrySet());

		Collections.sort(tmplist, new Comparator() {
			@Override
			public int compare(final Object o1, final Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		List list = new ArrayList<Answer>();
		for (Iterator it = tmplist.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			list.add(entry.getKey());
		}

		return list;
	}

}
