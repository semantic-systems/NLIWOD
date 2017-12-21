package org.aksw.hawk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GerbilFinalResponse {
	private Logger log = LoggerFactory.getLogger(GerbilFinalResponse.class);
	private List<GerbilResponseBuilder> questions;
	
	public GerbilFinalResponse(){
		questions = new ArrayList<>();
	}
	
	public GerbilFinalResponse setQuestions(HAWKQuestion answer){
		GerbilResponseBuilder responseToGerbil = new GerbilResponseBuilder();
		responseToGerbil.setId(answer.getId());
		//responseToGerbil.setAnswertype(answer.getAnswerType());
		responseToGerbil.setQuery(answer.getSparqlQuery("en"));
		log.info("query: " + responseToGerbil.getQuery());
		responseToGerbil.setQuestion(answer);
		responseToGerbil.setAnswerVec(answer);
		this.questions.add(responseToGerbil);
		log.info("GerbilQA object: " + this.toString());
		return this;
	}
	
	public List<GerbilResponseBuilder> getQuestions(){
		return questions;
	}
	
	@Override
	public String toString() {
		return "\n    questions: " + Objects.toString(questions);
	}
}
