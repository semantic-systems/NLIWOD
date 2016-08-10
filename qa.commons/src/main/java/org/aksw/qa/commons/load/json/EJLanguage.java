package org.aksw.qa.commons.load.json;

import java.util.Vector;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EJLanguage {

	@JsonProperty("SPARQL")
	private String sparql;
	/**
	 * Do not set this attribute for a ExtendedJson-like json
	 */
	private String pseudo;
	/**
	 * Do not set following attributes for a QALD-like json.
	 */

	private String language="en";
	private String question;
	private String keywords="";
	private String schemaless;
	private Vector<String> annotations;

	public EJLanguage() {
		this.annotations = new Vector<>();
	}

	@Override
	public String toString() {
		return "\n    SPARQL :" + sparql + "\n    Pseudo: " + pseudo + "\n    Language: " + language + "\n    Question: " + question + "\n    Keywords: " + keywords + "\n    Schemaless: " + schemaless
		        + "\n    Annotations: " + annotations.toString();
	}

	public String getSparql() {
		return sparql;
	}

	public String getLanguage() {
		return language;
	}

	public String getQuestion() {
		return question;
	}

	public String getKeywords() {
		return keywords;
	}

	public String getSchemaless() {
		return schemaless;
	}

	public Vector<String> getAnnotations() {
		return annotations;
	}

	public EJLanguage setSparql(final String sparql) {
		this.sparql = sparql;
		return this;
	}

	/**
	 * Do not set this attribute for a QALD-like json.
	 * 
	 */
	public EJLanguage setLanguage(final String language) {
		this.language = language;
		return this;
	}

	/**
	 * Do not set this attribute for a QALD-like json.
	 */
	public EJLanguage setQuestion(final String question) {
		this.question = question;
		return this;
	}

	/**
	 * Do not set this attribute for a QALD-like json.
	 */
	public EJLanguage setKeywords(final String keywords) {
		this.keywords = keywords;
		return this;
	}

	/**
	 * Do not set this attribute for a QALD-like json.
	 */
	public EJLanguage setSchemaless(final String schemaless) {
		this.schemaless = schemaless;
		return this;
	}

	/**
	 * Do not set this attribute for a QALD-like json.
	 */
	public EJLanguage setAnnotations(final Vector<String> annotations) {
		this.annotations = annotations;
		return this;
	}

	public String getPseudo() {
		return pseudo;
	}

	/**
	 * Do not set this property for a ExtendedJson-like json
	 */
	public EJLanguage setPseudo(final String pseudo) {
		this.pseudo = pseudo;
		return this;
	}

}