package org.aksw.qa.commons.datastructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IQuestion {

	@Override
	public String toString();

	public void setValue(String valDescriptor, String val);

	public String getId();

	public void setId(String id);

	public String getAnswerType();

	public void setAnswerType(String answerType);

	public String getPseudoSparqlQuery();

	public void setPseudoSparqlQuery(String pseudoSparqlQuery);

	public String getSparqlQuery();

	public void setSparqlQuery(String sparqlQuery);

	public Boolean getAggregation();

	public void setAggregation(Boolean aggregation);

	public Boolean getOnlydbo();

	public void setOnlydbo(Boolean onlydbo);

	public Boolean getOutOfScope();

	public void setOutOfScope(Boolean outOfScope);

	public Boolean getHybrid();

	public void setHybrid(Boolean hybrid);

	public Map<String, String> getLanguageToQuestion();

	public void setLanguageToQuestion(Map<String, String> languageToQuestion);

	public Map<String, List<String>> getLanguageToKeywords();

	public void setLanguageToKeywords(Map<String, List<String>> languageToKeywords);

	public Set<String> getGoldenAnswers();

	public void setGoldenAnswers(Set<String> goldenAnswers);

}