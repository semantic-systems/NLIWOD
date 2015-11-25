package org.aksw.qa.commons.datastructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.ext.com.google.common.collect.Sets;

public class Question {

	public Integer id;
	public String answerType;
	public String pseudoSparqlQuery;
	public String sparqlQuery;
	public Boolean aggregation;
	public Boolean onlydbo;
	public Boolean outOfScope;
	public Boolean hybrid;
	public Map<String, String> languageToQuestion = Maps.newLinkedHashMap();
	public Map<String, List<String>> languageToKeywords = Maps.newLinkedHashMap();
	public Set<String> goldenAnswers = Sets.newHashSet();

	@Override
	public String toString() {
		return "Question [id=" + id + ", answerType=" + answerType + ", pseudoSparqlQuery=" + pseudoSparqlQuery + ", sparqlQuery=" + sparqlQuery + ", aggregation=" + aggregation + ", onlydbo="
				+ onlydbo + ", outOfScope=" + outOfScope + ", hybrid=" + hybrid + ", languageToQuestion=" + languageToQuestion + ", languageToKeywords=" + languageToKeywords + ", goldenAnswers="
				+ goldenAnswers + "]";
	}

}
