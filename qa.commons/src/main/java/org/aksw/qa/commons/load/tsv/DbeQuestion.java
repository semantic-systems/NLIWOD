package org.aksw.qa.commons.load.tsv;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;

public class DbeQuestion implements IQuestion {
	
	private String id;
	private Map<String, String> languageToQuestion ;
	private Map<String, List<String>> languageToKeywords;
	private Set<String> goldenAnswers;
	

	@Override
	public void setValue(String valDescriptor, String val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setId(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getAnswerType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAnswerType(String answerType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getPseudoSparqlQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPseudoSparqlQuery(String pseudoSparqlQuery) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSparqlQuery() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSparqlQuery(String sparqlQuery) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean getAggregation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAggregation(Boolean aggregation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean getOnlydbo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOnlydbo(Boolean onlydbo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean getOutOfScope() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setOutOfScope(Boolean outOfScope) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Boolean getHybrid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHybrid(Boolean hybrid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, String> getLanguageToQuestion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLanguageToQuestion(Map<String, String> languageToQuestion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, List<String>> getLanguageToKeywords() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLanguageToKeywords(Map<String, List<String>> languageToKeywords) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getGoldenAnswers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setGoldenAnswers(Set<String> goldenAnswers) {
		// TODO Auto-generated method stub
		
	}

}