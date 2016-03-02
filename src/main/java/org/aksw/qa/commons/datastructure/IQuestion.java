package org.aksw.qa.commons.datastructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IQuestion {

	public abstract String toString();

	public abstract void setValue(String valDescriptor, String val);

	public abstract Integer getId();

	public abstract void setId(Integer id);

	public abstract String getAnswerType();

	public abstract void setAnswerType(String answerType);

	public abstract String getPseudoSparqlQuery();

	public abstract void setPseudoSparqlQuery(String pseudoSparqlQuery);

	public abstract String getSparqlQuery();

	public abstract void setSparqlQuery(String sparqlQuery);

	public abstract Boolean getAggregation();

	public abstract void setAggregation(Boolean aggregation);

	public abstract Boolean getOnlydbo();

	public abstract void setOnlydbo(Boolean onlydbo);

	public abstract Boolean getOutOfScope();

	public abstract void setOutOfScope(Boolean outOfScope);

	public abstract Boolean getHybrid();

	public abstract void setHybrid(Boolean hybrid);

	public abstract Map<String, String> getLanguageToQuestion();

	public abstract void setLanguageToQuestion(Map<String, String> languageToQuestion);

	public abstract Map<String, List<String>> getLanguageToKeywords();

	public abstract void setLanguageToKeywords(Map<String, List<String>> languageToKeywords);

	public abstract Set<String> getGoldenAnswers();

	public abstract void setGoldenAnswers(Set<String> goldenAnswers);

}