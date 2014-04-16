package org.aksw.hawk.module;

import com.hp.hpl.jena.vocabulary.RDF;

public class SimpleModule extends Module {
	public void addStatement(String subject, String predicate, String object) {
		// TODO if label which can be URI is not promising use string label
		WhereClause wc = null;
		if (predicate.equals(RDF.type.getURI())) {
			String dboTerm = dboIndex.search(object);
			if (dboTerm != null) {
				wc = new WhereClause(subject, predicate, dboTerm);
			} else {
				wc = new WhereClause(subject, predicate, object);
			}
		} else if (predicate.equals("IS")) {
			// skip since subject is var and object is a dbpedia resource
			wc = new WhereClause(subject, predicate, object);
		} else {
			String dboTerm = dboIndex.search(predicate);
			if (dboTerm != null) {
				wc = new WhereClause(subject, dboTerm, object);
			} else {
				wc = new WhereClause(subject, predicate, object);
			}
		}
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
