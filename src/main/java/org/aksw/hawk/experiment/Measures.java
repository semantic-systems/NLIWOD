package org.aksw.hawk.experiment;

import java.util.List;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.EvalObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Measures {
	static Logger log = LoggerFactory.getLogger(Measures.class);

	public static List<EvalObj> measure(List<Set<RDFNode>> rankedAnswerSet, Question q, int maxK) {
		// calculate precision, recall, f1 measure for each answer
		List<EvalObj> list = Lists.newArrayList();
		for (Set<RDFNode> answer : rankedAnswerSet) {
			double precision = QALD4_EvaluationUtils.precision(answer, q);
			double recall = QALD4_EvaluationUtils.recall(answer, q);
			double fMeasure = QALD4_EvaluationUtils.fMeasure(answer, q);

			log.debug("Measure @" + (list.size() + 1) + "P=" + precision + " R=" + recall + " F=" + fMeasure);
			list.add(new EvalObj(q.id, q.languageToQuestion.get("en"), fMeasure, precision, recall, "Measure @" + (list.size() + 1)));

			// only calculate top k measures
			if (list.size() > maxK) {
				break;
			}
		}
		return list;
	}
}
