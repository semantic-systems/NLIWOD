package org.aksw.hawk.module;

class WhereClause {
	public WhereClause() {
	}

	public WhereClause(String s, String p, String o) {
		super();
		String sTmp = s;
		sTmp = sTmp.startsWith("http") ? "<" + sTmp + ">" : sTmp;
		String pTmp = p;
		pTmp = pTmp.startsWith("http") ? "<" + pTmp + ">" : pTmp;
		String oTmp = o;
		oTmp = oTmp.startsWith("http") ? "<" + oTmp + ">" : oTmp;
		this.s = sTmp;
		this.p = pTmp;
		this.o = oTmp;
	}

	public String s;
	public String p;
	public String o;

	@Override
	public String toString() {
		return s + " " + p + " " + o + " .";
	}
}