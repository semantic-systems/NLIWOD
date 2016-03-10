package org.aksw.hawk.datastructures;

import java.util.ArrayList;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;

public class HAWKQuestionFactory {

	public static HAWKQuestion createInstance(IQuestion q) {
		HAWKQuestion hq = new HAWKQuestion();

		hq.setId(q.getId());
		hq.setAnswerType(q.getAnswerType());
		hq.setPseudoSparqlQuery(q.getPseudoSparqlQuery());
		hq.setSparqlQuery(q.getSparqlQuery());
		hq.setAggregation(q.getAggregation());
		hq.setOnlydbo(q.getOnlydbo());
		hq.setOutOfScope(q.getOutOfScope());
		hq.setHybrid(q.getHybrid());

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
