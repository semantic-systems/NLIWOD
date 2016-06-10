package org.aksw.qa.commons.load.extended;

import java.util.Vector;

public class EJMetadata {
	private String answertype;
	private Vector<String> answerItemType;

	public EJMetadata() {
		this.answerItemType = new Vector<>();
	}

	public String getAnswertype() {
		return answertype;
	}

	public EJMetadata setAnswertype(final String answertype) {
		this.answertype = answertype;
		return this;
	}

	public Vector<String> getAnswerItemType() {
		return answerItemType;
	}

	public EJMetadata setAnswerItemType(final Vector<String> answeritemtype) {
		this.answerItemType = answeritemtype;
		return this;
	}

	public EJMetadata addAnswerItemType(final String answeritemtype) {
		this.answerItemType.add(answeritemtype);
		return this;
	}

}
