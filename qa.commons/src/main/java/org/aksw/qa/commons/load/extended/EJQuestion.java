package org.aksw.qa.commons.load.extended;

import java.util.Vector;

public class EJQuestion {
	private String language;
	private String string;
	private String keywords;
	private Vector<EJAnnotation> annotations;

	public EJQuestion() {
		this.annotations = new Vector<>();
	}

	public String getLanguage() {
		return language;
	}

	public EJQuestion setLanguage(final String language) {
		this.language = language;
		return this;
	}

	public String getString() {
		return string;
	}

	public EJQuestion setString(final String string) {
		this.string = string;
		return this;
	}

	public String getKeywords() {
		return keywords;
	}

	public EJQuestion setKeywords(final String keywords) {
		this.keywords = keywords;
		return this;
	}

	public Vector<EJAnnotation> getAnnotations() {
		return annotations;
	}

	public EJQuestion setAnnotations(final Vector<EJAnnotation> annotations) {
		this.annotations = annotations;
		return this;
	}

	public EJQuestion addAnnotation(final EJAnnotation annotation) {
		this.annotations.add(annotation);
		return this;
	}

}
