package org.aksw.hawk.module;

import org.aksw.hawk.nlp.posTree.MutableTreeNode;

public class ConstraintModule extends Module {
	public ConstraintModule(MutableTreeNode constraintPredicate, MutableTreeNode constraintObject) {
		// e.g. (assassin, dbr:MLK)
		// TODO if constraintObject is not a dbr term do something
		// TODO only one kind of BGP supported so far
		WhereClause wc = new WhereClause();
		wc.s = "?xo1";
		wc.p = constraintPredicate.label;
		wc.o = constraintObject.label;
		statementList.add(wc);
		
		String dboTerm = dboIndex.search(constraintPredicate.label);
		log.debug("DBOTerm: " + dboTerm);
		
		if (dboTerm != null) {
			wc = new WhereClause();
			wc.s = "?xo1";
			wc.p = dboTerm;
			wc.o = constraintObject.label;
			statementList.add(wc);
		}
	}
}
