package org.aksw.hawk.module;

public class SimpleModule extends Module {
	public void addStatement(WhereClause wc) {
		statementList.add(wc);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (WhereClause x : statementList) {
			sb.append("\t" + x.s + " " + x.p + " " + x.o + "\n");
		}
		return sb.toString();
	}
}
