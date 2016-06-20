package org.aksw.qa.commons.load.extended;

import java.util.HashMap;
import java.util.Vector;

public class EJAnswers {
	private EJHead head;
	private Vector<HashMap<String, EJBinding>> bindings;
	private String confidence;

	public EJAnswers() {
		this.bindings = new Vector<>();
		this.head = new EJHead();
	}

	public EJHead getHead() {
		return head;
	}

	public String getConfidence() {
		return confidence;
	}

	public EJAnswers setConfidence(final String confidence) {
		this.confidence = confidence;
		return this;
	}

	public Vector<HashMap<String, EJBinding>> getBindings() {
		return bindings;
	}

	public EJAnswers setBindings(final Vector<HashMap<String, EJBinding>> bindings) {
		this.bindings = bindings;
		return this;
	}

	public EJAnswers setHead(final EJHead head) {
		this.head = head;
		return this;
	}

	public EJAnswers addBindings(final HashMap<String, EJBinding> bindingEntry) {
		this.bindings.add(bindingEntry);
		return this;
	}

}
