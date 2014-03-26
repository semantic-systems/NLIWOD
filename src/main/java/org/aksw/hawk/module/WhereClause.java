package org.aksw.hawk.module;

class WhereClause {
	public String s;
	public String p;
	public String o;

	@Override
	public String toString() {
		return s + " " + p + " " + o;
	}
}