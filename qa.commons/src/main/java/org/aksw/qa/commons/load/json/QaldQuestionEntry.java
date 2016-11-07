package org.aksw.qa.commons.load.json;

import java.util.Objects;
import java.util.Vector;

public class QaldQuestionEntry {

	private String id;
	private String answertype;
	private Boolean aggregation;
	private Boolean onlydbo;
	private Boolean hybrid;
	private Vector<QaldQuestion> question;
	private QaldQuery query;
	private Vector<EJAnswers> answers;

	public QaldQuestionEntry() {
		question = new Vector<>();
		answers = new Vector<>();
	}

	@Override
	public String toString() {
		return "\n  ID :" + id + "\n  Answertype: " + answertype + "\n  Aggregation: " + aggregation + "\n  Onlydbo: " + onlydbo + "\n  Hybrid: " + hybrid + "\n  Questions: "
		        + question.toString().replaceAll(",", "\n    ") + Objects.toString(query) + "\n  Answers: " + answers.toString().replaceAll(",", "\n    ");
	}

	public String getId() {
		return id;
	}

	public String getAnswertype() {
		return answertype;
	}

	public Boolean getAggregation() {
		return aggregation;
	}

	public Boolean getOnlydbo() {
		return onlydbo;
	}

	public Boolean getHybrid() {
		return hybrid;
	}

	public Vector<QaldQuestion> getQuestion() {
		return question;
	}

	public QaldQuery getQuery() {
		return query;
	}

	public Vector<EJAnswers> getAnswers() {
		return answers;
	}

	public QaldQuestionEntry setId(final String id) {
		this.id = id;
		return this;
	}

	public QaldQuestionEntry setAnswertype(final String answertype) {
		this.answertype = answertype;
		return this;
	}

	public QaldQuestionEntry setAggregation(final Boolean aggregation) {
		this.aggregation = aggregation;
		return this;
	}

	public QaldQuestionEntry setOnlydbo(final Boolean onlydbo) {
		this.onlydbo = onlydbo;
		return this;
	}

	public QaldQuestionEntry setHybrid(final Boolean hybrid) {
		this.hybrid = hybrid;
		return this;
	}

	public QaldQuestionEntry setQuestion(final Vector<QaldQuestion> question) {
		this.question = question;
		return this;
	}

	public QaldQuestionEntry setQuery(final QaldQuery query) {
		this.query = query;
		return this;
	}

	public QaldQuestionEntry setAnswers(final Vector<EJAnswers> answers) {
		this.answers = answers;
		return this;
	}

}
