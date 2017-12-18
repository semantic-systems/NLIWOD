package org.aksw.hawk.util;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.load.json.EJAnswers;
import org.aksw.qa.commons.load.json.EJHead;
import org.aksw.qa.commons.load.json.EJLanguage;
import org.aksw.qa.commons.load.json.EJQuestion;
import org.apache.jena.query.QueryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GerbilResponseBuilder {
	private Logger log = LoggerFactory.getLogger(GerbilResponseBuilder.class);
	private String id;
	private String answertype;
	private Vector<Map<String,String>> question;
	private Map<String, String> query;
	private Vector<GerbilAnswer> answers;
	
	public GerbilResponseBuilder() {
		this.id = new String();
		this.answertype = new String();
		this.question = new Vector<>();
		this.query = new HashMap<>();
		this.answers = new Vector<>();
	}
	
	public GerbilResponseBuilder setId(final String ques) {
		this.id = ques;
		return this;
	}
	
	public GerbilResponseBuilder setQuestion(final HAWKQuestion q) {
		Map<String,String> gerbilQuestion = new HashMap<>();
		gerbilQuestion.put("language", q.getLanguageToQuestion().keySet().toArray()[0].toString());
		gerbilQuestion.put("string", q.getLanguageToQuestion().get("en"));
		this.question.add(gerbilQuestion);
		return this;
	}
	
	public GerbilResponseBuilder setQuery(final String query) {
		this.query.put("sparql", query);
		return this;
	}
	
	public void setAnswerVec(final HAWKQuestion q) {
		GerbilAnswer ans = new GerbilAnswer();
		ans.setHead(q);
		ans.setResults(q);
		this.answers.add(ans);
	}

	public void setAnswertype(final String answertype) {
		this.answertype =answertype;
	}

	public String getId() {
		return this.id;
	}

	public String getAnswertype() {
		return this.answertype;
	}
	
	public Map<String, String> getQuery() {
		return this.query;
	}
	
	public Vector<Map<String,String>> getQuestions(){
		return this.question;
	}

	public Vector<GerbilAnswer> getAnswers() {
		return this.answers;
	}
	
	@Override
	public String toString() {
		return "\n ID :" + id + "\n Answertype: " + answertype + "\n Query: " + Objects.toString(query) + "\n Question: " + Objects.toString(question) + "\n Answers: " + Objects.toString(answers);
	}

}
