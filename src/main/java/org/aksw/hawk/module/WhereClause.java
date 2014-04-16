package org.aksw.hawk.module;

class WhereClause {
	public WhereClause() {
	}

	public WhereClause(String s, String p, String o) {
		super();
		this.s = s;
		this.p = p;
		this.o = o;
	}

	public String s;
	public String p;
	public String o;

	@Override
	public String toString() {
		return s + " " + p + " " + o +" .";
	}
}