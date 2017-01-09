package org.aksw.hawk.datastructures;

import java.util.ArrayList;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.qald.QALD4_EvaluationUtils;

public class HAWKQuestionFactory {

	public static HAWKQuestion createInstance(IQuestion q) {
		HAWKQuestion hq = new HAWKQuestion();

		hq.setId(q.getId());
		hq.setAnswerType(q.getAnswerType());
		hq.setPseudoSparqlQuery(q.getPseudoSparqlQuery());
		hq.setSparqlQuery(q.getSparqlQuery());
		hq.setAggregation(Boolean.TRUE.equals(q.getAggregation()));
		hq.setOnlydbo(Boolean.TRUE.equals(q.getOnlydbo()));
		hq.setOutOfScope(Boolean.TRUE.equals(q.getOutOfScope()));
		hq.setHybrid(Boolean.TRUE.equals(q.getHybrid()));

		boolean b = QALD4_EvaluationUtils.isAskType(q.getSparqlQuery());
		b |= QALD4_EvaluationUtils.isAskType(q.getPseudoSparqlQuery());
		hq.setLoadedAsASKQuery(b);

		hq.setLanguageToQuestion(q.getLanguageToQuestion());
		hq.setLanguageToKeywords(q.getLanguageToKeywords());
		hq.setGoldenAnswers(q.getGoldenAnswers());
		return hq;
	}

	public static List<HAWKQuestion> createInstances(List<IQuestion> qList) {
		ArrayList<HAWKQuestion> hq = new ArrayList<HAWKQuestion>();
		for (IQuestion q : qList) {
			hq.add(HAWKQuestionFactory.createInstance(q));
		}
		return hq;
	}

}
