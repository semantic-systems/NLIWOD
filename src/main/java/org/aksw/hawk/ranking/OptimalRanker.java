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
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * optimal in a sense of fmeasure
 * 
 * @author ricardousbeck
 *
 */
public class OptimalRanker implements Ranking {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<Set<RDFNode>> rank(List<Answer> answers, Question q) {
		// Compare to set of resources from benchmark
		// save score for for answerSet with fmeasure > 0
		Map<Set<RDFNode>, Double> buckets = Maps.newHashMap();

		for (Answer answer : answers) {
			Set<RDFNode> answerSet = answer.answerSet;
			double fMeasure = QALD4_EvaluationUtils.fMeasure(answerSet, q);

			if (fMeasure > 0) {

				buckets.put(answerSet, fMeasure);
			}
		}

		// sort according to entries in buckets
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
