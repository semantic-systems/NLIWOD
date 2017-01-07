package org.aksw.qa.commons.load.json;

public class EJQuestionEntry {

	private EJQuestion question;

	public EJQuestion getQuestion() {
		if (this.question == null) {
			question = new EJQuestion();
		}
		return question;
	}

	public void setQuestion(final EJQuestion question) {
		this.question = question;
	}

}
