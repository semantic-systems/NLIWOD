package org.aksw.hawk.ranking;

import java.util.List;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;

public interface Ranking {

	public List<Answer> rank(List<Answer> answers, HAWKQuestion q);

}
