package org.aksw.qa.commons.load.json;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EJAnswers {
	private EJHead head;
	private EJResults results;
	/**
	 * Do not set this attribute for a QALD-like json.
	 *
	 */
	private String confidence;
	private Boolean isTrue;

	public EJAnswers() {
		this.confidence = "";
	}
	
	@JsonCreator
	public static EJAnswers factory(String json){
		try {
			return new ObjectMapper().readValue(json, EJAnswers.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String toString() {
		return "\n    Head: " + Objects.toString(head) + "\n    Results: " + Objects.toString(results) + "\n    Confidence: " + confidence + "\n    Boolean: " + isTrue;
	}

	public EJResults getResults() {
		return results;
	}

	public EJHead getHead() {
		return head;
	}

	public String getConfidence() {
		return confidence;
	}

	public Boolean getBoolean() {
		return isTrue;
	}

	public EJAnswers setHead(final EJHead head) {
		this.head = head;
		return this;
	}

	/**
	 * Do not set this attribute for a QALD-like json.
	 *
	 */
	public EJAnswers setConfidence(final String confidence) {
		this.confidence = confidence;
		return this;
	}

	public EJAnswers setBoolean(final Boolean isTrue) {
		this.isTrue = isTrue;
		return this;
	}

	public EJAnswers setResults(final EJResults results) {
		this.results = results;
		return this;

	}

}
