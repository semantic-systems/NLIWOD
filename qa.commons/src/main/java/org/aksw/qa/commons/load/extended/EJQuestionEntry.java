package org.aksw.qa.commons.load.extended;

public class EJQuestionEntry {
	private Integer id;
	private EJMetadata metadata;
	private EJQuestion question;
	private EJQuery query;
	private EJAnswers answers;

	public EJQuestionEntry() {
		this.metadata = new EJMetadata();
		this.question = new EJQuestion();
		this.query = new EJQuery();
		this.answers = new EJAnswers();
	}

	public Integer getId() {
		return id;
	}

	public EJQuestionEntry setId(final Integer id) {
		this.id = id;
		return this;
	}

	public EJMetadata getMetadata() {
		return metadata;
	}

	public EJQuestionEntry setMetadata(final EJMetadata metadata) {
		this.metadata = metadata;
		return this;
	}

	public EJQuestion getQuestion() {
		return question;
	}

	public EJQuestionEntry setQuestion(final EJQuestion question) {
		this.question = question;
		return this;
	}

	public EJQuery getQuery() {
		return query;
	}

	public EJQuestionEntry setQuery(final EJQuery query) {
		this.query = query;
		return this;
	}

	public EJAnswers getAnswers() {
		return answers;
	}

	public EJQuestionEntry setAnswers(final EJAnswers answers) {
		this.answers = answers;
		return this;
	}

}
