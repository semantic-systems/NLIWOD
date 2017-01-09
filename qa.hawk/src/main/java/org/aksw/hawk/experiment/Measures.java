package org.aksw.hawk.experiment;

import java.util.List;
import java.util.Set;

import org.aksw.hawk.controller.EvalObj;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.qald.QALD4_EvaluationUtils;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class Measures {
	static Logger log = LoggerFactory.getLogger(Measures.class);

	public static List<EvalObj> measure(final List<Answer> rankedAnswer, final HAWKQuestion q, final int maxK) {
		// calculate precision, recall, f1 measure for each answer
		List<EvalObj> list = Lists.newArrayList();
		// omment=Measure @1, fmax=0.8, pmax=0.6666666666666666, rmax=1.0,
		// question=Which recipients of the Victoria Cross died in the Battle of
		// Arnhem?, id=305]
		// EvalObj [comment=Measure @2, fmax=0.0, pmax=0.0, rmax=0.0,
		// question=Which recipients of the Victoria Cross died in the Battle of
		// Arnhem?, id=305]
		// EvalObj [comment=Measure @3, fmax=0.0, pmax=0.0, rmax=0.0,
		// question=Which recipients of the Victoria Cross died in the Battle of
		// Arnhem?, id=305]
		// EvalObj [comment=Measure @4, fmax=0.0, pmax=0.0, rmax=0.0,
		// question=Which recipients of the Victoria Cross died in the Battle of
		// Arnhem?, id=305]
		// EvalObj [comment=Measure @5, fmax=0.0, pmax=0.0, rmax=0.0,
		// question=Which recipients of the Victoria Cross died in the Battle of
		// Arnhem?, id=305]
		// EvalObj [comment=Measure @6, fmax=0.5, pmax=0.5, rmax=0.5,
		// question=Which recipients of the Victoria Cross died in the Battle of
		// Arnhem?, id=305]
		// EvalObj [comment=Measure @7, fmax=0.0, pmax=0.0, rmax=0.0,
		// question=Which recipients of the Victoria Cross died in the Battle of
		// Arnhem?, id=305]
		// EvalObj [comment=Measure @8, fmax=0.28571428571428575, pmax=0.2,
		// rmax=0.5, question=Which recipients of the Victoria Cross died in the
		// Battle of Arnhem?, id=305]
		// EvalObj [comment=Measure @9, fmax=0.0, pmax=0.0, rmax=0.0,
		// question=Which recipients of the Victoria Cross died in the Battle of
		// Arnhem?, id=305]
		// EvalObj [comment=Measure @10, fmax=0.3636363636363636,
		// pmax=0.2222222222222222, rmax=1.0, question=Which recipie
		for (Answer answer : rankedAnswer) {
			Set<RDFNode> answerSet = answer.answerSet;
			double precision = QALD4_EvaluationUtils.precision(answerSet, q);
			double recall = QALD4_EvaluationUtils.recall(answerSet, q);
			double fMeasure = QALD4_EvaluationUtils.fMeasure(answerSet, q);

			log.debug("Measure @" + (list.size() + 1) + "P=" + precision + " R=" + recall + " F=" + fMeasure);
			list.add(new EvalObj(q.getId(), q.getLanguageToQuestion().get("en"), fMeasure, precision, recall, "Measure @" + (list.size() + 1), answer));

			// only calculate top k measures
			if (list.size() > maxK) {
				break;
			}
		}
		return list;
	}
}
