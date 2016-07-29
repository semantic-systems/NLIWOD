package org.aksw.qa.commons.load.json;

import java.util.Vector;

public class EJHead {
	private Vector<String> vars;
	private Vector<String> link;

	public EJHead() {
		vars = new Vector<>();
		link = new Vector<>();
	}

	@Override
	public String toString() {
		return "\n      Vars: " + vars.toString() + "\n      Links: " + link.toString();
	}

	public Vector<String> getVars() {
		return vars;
	}

	public Vector<String> getLink() {
		return link;
	}

	public EJHead setVars(final Vector<String> vars) {
		this.vars = vars;
		return this;
	}

	public EJHead setLink(final Vector<String> link) {
		this.link = link;
		return this;
	}

}
