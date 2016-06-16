package org.aksw.qa.commons.load.extended;

import java.util.Vector;

public class ExtendedJson {

	private EJDataset dataset;
	private Vector<EJQuestionEntry> questions;

	public ExtendedJson() {
		this.dataset = new EJDataset();
		this.questions = new Vector<>();
	}

	public EJDataset getDataset() {
		return dataset;
	}

	public ExtendedJson setDataset(final EJDataset dataset) {
		this.dataset = dataset;
		return this;
	}

	public Vector<EJQuestionEntry> getQuestions() {
		return questions;
	}

	public ExtendedJson setQuestions(final Vector<EJQuestionEntry> questions) {
		this.questions = questions;
		return this;
	}

	public ExtendedJson addQuestions(final EJQuestionEntry questions) {
		this.questions.add(questions);
		return this;
	}
}
