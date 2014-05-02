package org.aksw.hawk.module;

class WhereClause {
	public WhereClause() {
	}

	public WhereClause(String s, String p, String o) {
		super();
		String sTmp = s;
		if (sTmp.startsWith("http://")) {
			sTmp = "<" + s + ">";
		} else if (sTmp.startsWith("?")) {
			sTmp = s;
		} else {
			sTmp = "<" + s + ">";
		}
		String pTmp = p;
		if (pTmp.startsWith("http://")) {
			pTmp = "<" + p + ">";
		} else if (pTmp.startsWith("?")) {
			pTmp = p;
		} else {
			pTmp = "<" + p + ">";
		}
		String oTmp = o;
		if (oTmp.startsWith("http://")) {
			oTmp = "<" + o + ">";
		} else if (oTmp.startsWith("?")) {
			oTmp = o;
		} else {
			//TODO escape whitespaces
			oTmp = "<" + o + ">";
		}
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