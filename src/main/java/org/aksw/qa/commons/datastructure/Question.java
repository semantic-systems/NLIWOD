package org.aksw.qa.commons.datastructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.utils.CollectionUtils;

public class Question {

	public Integer id;
	public String answerType;
	public String pseudoSparqlQuery;
	public String sparqlQuery;
	public Boolean aggregation;
	public Boolean onlydbo;
	public Boolean outOfScope;
	public Boolean hybrid;
	public Map<String, String> languageToQuestion = CollectionUtils.newLinkedHashMap();
	public Map<String, List<String>> languageToKeywords = CollectionUtils.newLinkedHashMap();
	public Set<String> goldenAnswers = CollectionUtils.newHashSet();

	@Override
	public String toString() {
		return "Question [id=" + id + ", answerType=" + answerType + ", pseudoSparqlQuery=" + pseudoSparqlQuery + ", sparqlQuery=" + sparqlQuery + ", aggregation=" + aggregation + ", onlydbo="
		        + onlydbo + ", outOfScope=" + outOfScope + ", hybrid=" + hybrid + ", languageToQuestion=" + languageToQuestion + ", languageToKeywords=" + languageToKeywords + ", goldenAnswers="
		        + goldenAnswers + "]";
	}

}
