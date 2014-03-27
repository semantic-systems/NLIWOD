package org.aksw.hawk.module;

import org.aksw.hawk.index.DBOIndex;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;

public class ProjectionConstraintModule extends Module {

	public ProjectionConstraintModule(MutableTreeNode mutableTreeNode) {
		DBOIndex dboIndex = new DBOIndex();
		String dboTerm = dboIndex.search(mutableTreeNode.label);
		log.debug("DBOTerm: " + dboTerm);

		WhereClause wc = new WhereClause();
		wc.s = "?uri";
		wc.p = mutableTreeNode.label;
		wc.o = "?xo1";
		statementList.add(wc);

		wc = new WhereClause();
		wc.s = "?uri";
		wc.p = dboTerm;
		wc.o = "?xo1";
		statementList.add(wc);
	}

}
