package org.aksw.hawk.controller;

import java.util.Set;

import org.aksw.hawk.querybuilding.SPARQLQuery;

import com.hp.hpl.jena.rdf.model.RDFNode;
//FIXME mach eine ordentliche toString()
public class Answer implements Comparable<Answer> {

	public Set<RDFNode> answerSet;
	public SPARQLQuery query;
	public Double score = 1.0;
	public String question;
	public Integer question_id;
	public String queryString;

	@Override
	public int compareTo(Answer a) {
		return Double.compare(this.score, a.score);
	}

}