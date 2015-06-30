package org.aksw.hawk.ranking;

import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;

public interface Ranking {

	public List<Answer> rank(List<Answer> answers, Question q);
}
