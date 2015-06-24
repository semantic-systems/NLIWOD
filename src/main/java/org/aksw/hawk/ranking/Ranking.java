package org.aksw.hawk.ranking;

import java.util.List;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;

import com.hp.hpl.jena.rdf.model.RDFNode;

public interface Ranking {

	public List<Set<RDFNode>> rank(List<Answer> answers, Question q);
}
