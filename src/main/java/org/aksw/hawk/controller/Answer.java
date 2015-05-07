package org.aksw.hawk.controller;

import java.util.Set;

import org.aksw.hawk.querybuilding.SPARQLQuery;

import com.hp.hpl.jena.rdf.model.RDFNode;


public class Answer {

	public Set<RDFNode> answerSet;
	public SPARQLQuery query;
	public String question;
	public String question_id;
}