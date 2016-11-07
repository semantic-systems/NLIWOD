package org.aksw.qa.commons.load.json;

import java.util.Objects;
import java.util.Vector;

import org.apache.jena.query.QueryFactory;

public class EJQuestion {
	private String id;
	private String answertype;
	private String confidence;

	private Vector<String> answeritemtype;
	private Vector<EJLanguage> language;
	private EJAnswers answers;

	public EJQuestion() {
		this.answeritemtype = new Vector<>();
		this.language = new Vector<>();

	}

	public EJQuestion addAnsweritemtype(final String name) {
		this.answeritemtype.add(name);
		return this;
	}

	@Override
	public String toString() {
		return "\n  ID :" + id + "\n  Answertype: " + answertype + "\n  Confidence: " + confidence + "\n  Answeritemtype: " + answeritemtype.toString() + "\n  Language: " + Objects.toString(language)
		        + "\n  Answers: " + Objects.toString(answers);
	}

	public String getId() {
		return id;
	}

	public String getAnswertype() {
		return answertype;
	}

	public String getConfidence() {
		return confidence;
	}

	public Vector<String> getAnsweritemtype() {
		return answeritemtype;
	}

	public Vector<EJLanguage> getLanguage() {
		return language;
	}

	public EJAnswers getAnswers() {
		return answers;
	}

	public EJQuestion setName(final String id) {
		this.id = id;
		return this;
	}

	public EJQuestion setId(final String id) {
		this.id = id;
		return this;
	}

	public EJQuestion setAnswertype(final String answertype) {
		this.answertype = answertype;
		return this;
	}

	public EJQuestion setConfidence(final String confidence) {
		this.confidence = confidence;
		return this;
	}

	public EJQuestion setAnsweritemtype(final Vector<String> answeritemtype) {
		this.answeritemtype = answeritemtype;
		return this;
	}

	public EJQuestion setLanguage(final Vector<EJLanguage> language) {
		this.language = language;
		for(EJLanguage lang : this.language){
			try{
				QueryFactory.create(lang.getSparql());
			}catch(Exception e){
				this.language.remove(lang);
			}
		}
		return this;
	}

	public EJQuestion setAnswers(final EJAnswers answers) {
		this.answers = answers;
		return this;
	}

}
