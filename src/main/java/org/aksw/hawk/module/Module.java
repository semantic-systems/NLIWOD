package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Module {
	public List<WhereClause> statementList = new ArrayList<>();
	Logger log = LoggerFactory.getLogger(getClass());

	public Module(MutableTreeNode mutableTreeNode, MutableTreeNode node, Question q) {
		buildRDFTypeStatement(mutableTreeNode, node);
		buildPredicateStatement(mutableTreeNode, node);

	}

	private void buildPredicateStatement(MutableTreeNode mutableTreeNode, MutableTreeNode node) {
		WhereClause wc = new WhereClause();
		wc.p = mutableTreeNode.label;
		wc.o = node.label;

		log.debug("\t\t\t" + wc);
		statementList.add(wc);
	}

	private void buildRDFTypeStatement(MutableTreeNode mutableTreeNode, MutableTreeNode node) {
		WhereClause wc = new WhereClause();
		wc.p = "rdf:type";
		wc.o = mutableTreeNode.label;
		log.debug("\t\t\t" + wc);
		statementList.add(wc);
	}
}
