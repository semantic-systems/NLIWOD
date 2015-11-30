package org.aksw.qa.commons.datastructure;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Question {

	public Integer id;
	public String answerType;
	public String pseudoSparqlQuery;
	public String sparqlQuery;
	public Boolean aggregation;
	public Boolean onlydbo;
	public Boolean outOfScope;
	public Boolean hybrid;
	public Map<String, String> languageToQuestion = new LinkedHashMap<String, String>();
	public Map<String, List<String>> languageToKeywords = new LinkedHashMap<String, List<String>>();
	public Set<String> goldenAnswers = new HashSet<String>();

	@Override
	public String toString() {
		return "Question [id=" + id + ", answerType=" + answerType + ", pseudoSparqlQuery=" + pseudoSparqlQuery + ", sparqlQuery=" + sparqlQuery + ", aggregation=" + aggregation + ", onlydbo="
				+ onlydbo + ", outOfScope=" + outOfScope + ", hybrid=" + hybrid + ", languageToQuestion=" + languageToQuestion + ", languageToKeywords=" + languageToKeywords + ", goldenAnswers="
				+ goldenAnswers + "]";
	}

}
