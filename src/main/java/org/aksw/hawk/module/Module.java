package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public abstract class Module {
	public List<WhereClause> statementList = new ArrayList<>();
	Logger log = LoggerFactory.getLogger(getClass());

//	public Module(MutableTreeNode mutableTreeNode, MutableTreeNode node, Question q) {
//		buildRDFTypeStatement(mutableTreeNode, node);
//		buildPredicateStatement(mutableTreeNode, node);
//
//	}
//
//	private void buildPredicateStatement(MutableTreeNode mutableTreeNode, MutableTreeNode node) {
//		WhereClause wc = new WhereClause();
//		wc.p = mutableTreeNode.label;
//		wc.o = node.label;
//
//		log.debug("\t\t\t" + wc);
//		statementList.add(wc);
//	}
//
//	private void buildRDFTypeStatement(MutableTreeNode mutableTreeNode, MutableTreeNode node) {
//		WhereClause wc = new WhereClause();
//		wc.p = "rdf:type";
//		wc.o = mutableTreeNode.label;
//		log.debug("\t\t\t" + wc);
//		statementList.add(wc);
//	}
	public String toString(){
		return Joiner.on("\n").join(statementList);
	}
}
