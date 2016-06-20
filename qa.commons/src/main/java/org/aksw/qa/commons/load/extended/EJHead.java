package org.aksw.qa.commons.load.extended;

import java.util.HashSet;
import java.util.Set;

public class EJHead {
	private Set<String> vars;

	public EJHead() {
		vars = new HashSet<>();
	}

	public Set<String> getVars() {
		return vars;
	}

	public EJHead setVars(final Set<String> vars) {
		this.vars = vars;
		return this;
	}

	public EJHead addVar(final String var) {
		vars.add(var);
		return this;
	}
}
