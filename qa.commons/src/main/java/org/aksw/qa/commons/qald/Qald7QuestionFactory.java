package org.aksw.qa.commons.qald;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;

public class Qald7QuestionFactory {

	public static Qald7Question createInstance(final IQuestion q) {
		Qald7Question q7 = new Qald7Question();
		q7.setAggregation(q.getAggregation());
		q7.setAnswerType(q.getAnswerType());
		q7.setGoldenAnswers(q.getGoldenAnswers());
		q7.setHybrid(q.getHybrid());
		q7.setId(q.getId());
		q7.setLanguageToKeywords(q.getLanguageToKeywords());
		q7.setLanguageToQuestion(q.getLanguageToQuestion());
		q7.setOnlydbo(q.getOnlydbo());
		q7.setOutOfScope(q.getOutOfScope());
		q7.setPseudoSparqlQuery(q.getPseudoSparqlQuery());
		q7.setSparqlQuery(q.getSparqlQuery());
		return q7;
	}

	public static List<Qald7Question> createInstances(final Collection<IQuestion> questions) {
		List<Qald7Question> ret = new ArrayList<>();
		for (IQuestion q : questions) {
			ret.add(Qald7QuestionFactory.createInstance(q));
		}
		return ret;
	}

}
