package org.aksw.hawk.module;

import org.aksw.hawk.nlp.posTree.MutableTreeNode;

public class ProjectionConstraintModule extends Module {

	public ProjectionConstraintModule(MutableTreeNode mutableTreeNode) {
		WhereClause wc = new WhereClause();
		wc.s = "?uri";
		wc.p = mutableTreeNode.label;
		wc.o = "?xo1";
		statementList.add(wc);

		String dboTerm = dboIndex.search(mutableTreeNode.label);
		log.debug("DBOTerm: " + dboTerm);
		if (dboTerm != null) {
			wc = new WhereClause();
			wc.s = "?uri";
			wc.p = dboTerm;
			wc.o = "?xo1";
			statementList.add(wc);
		}
	}

}
