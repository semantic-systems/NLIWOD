package org.aksw.hawk.module;

import org.aksw.hawk.nlp.posTree.MutableTreeNode;

public class ProjectionConstraintModule extends Module {

	public ProjectionConstraintModule(MutableTreeNode mutableTreeNode) {
		// TODO make the BGP depend on the rdf:type of the projection variable
		WhereClause wc = new WhereClause();
		wc.s = "?xo1";
		wc.p = mutableTreeNode.label;
		wc.o = "?uri";
		statementList.add(wc);

		String dboTerm = dboIndex.search(mutableTreeNode.label);
		log.debug("DBOTerm: " + dboTerm);
		if (dboTerm != null) {
			wc = new WhereClause();
			wc.s = "?xo1";
			wc.p = dboTerm;
			wc.o = "?uri";
			statementList.add(wc);
		}
	}

}
