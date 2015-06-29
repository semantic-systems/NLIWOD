package org.aksw.hawk.experiment;

import java.util.List;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.controller.EvalObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Measures {
	static Logger log = LoggerFactory.getLogger(Measures.class);

	public static List<EvalObj> measure(List<Answer> rankedAnswer, Question q, int maxK) {
		// calculate precision, recall, f1 measure for each answer
		List<EvalObj> list = Lists.newArrayList();
		for (Answer answer : rankedAnswer) {
			Set<RDFNode> answerSet = answer.answerSet;
			double precision = QALD4_EvaluationUtils.precision(answerSet, q);
			double recall = QALD4_EvaluationUtils.recall(answerSet, q);
			double fMeasure = QALD4_EvaluationUtils.fMeasure(answerSet, q);

			log.debug("Measure @" + (list.size() + 1) + "P=" + precision + " R=" + recall + " F=" + fMeasure);
			list.add(new EvalObj(q.id, q.languageToQuestion.get("en"), fMeasure, precision, recall, "Measure @" + (list.size() + 1), answer));

			// only calculate top k measures
			if (list.size() > maxK) {
				break;
			}
		}
		return list;
	}
}
