package org.aksw.qa.commons.load.json;

import java.util.HashMap;
import java.util.Vector;

public class EJResults {
	private Vector<HashMap<String, EJBinding>> bindings;

	public EJResults() {
		bindings = new Vector<>();
	}

	public Vector<HashMap<String, EJBinding>> getBindings() {
		return bindings;
	}

	public EJResults setBindings(final Vector<HashMap<String, EJBinding>> bindings) {
		this.bindings = bindings;
		return this;
	}

	@Override
	public String toString() {
		return "\n      " + bindings.toString();
	}
}
