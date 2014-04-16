package org.aksw.hawk.module;

import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.hp.hpl.jena.vocabulary.RDF;

public class ModuleBuilder {
	Logger log = LoggerFactory.getLogger(ModuleBuilder.class);
	private List<Module> list;
	private boolean rootVisited = false;
	private int variableNumber = 1;

	public List<Module> build(Question q) {
		this.list = Lists.newArrayList();

		eulerTour(q.tree.getRoot());

		if (list != null) {
			log.debug("\n" + Joiner.on("\n").skipNulls().join(list));
		}
		return list;
	}

	// traverse tree in an euler tour
	private void eulerTour(MutableTreeNode root) {
		// visiting before going down
		visitBeforeGoingDown(root);
		// going left
		if (root.getChildren().size() > 0) {
			eulerTour(root.getChildren().get(0));
		}
		// visiting current node
		visitCurrent(root);
		// going right
		if (root.getChildren().size() > 1) {
			for (int i = 1; i < root.getChildren().size(); i++) {
				eulerTour(root.getChildren().get(i));
			}
		}
		// visiting going up
		visitGoingUp(root);
	}

	private void visitCurrent(MutableTreeNode node) {
		if (!rootVisited) {
			build(node);
		}
		if (node.parent == null) {
			rootVisited = true;
		}

	}

	private void visitBeforeGoingDown(MutableTreeNode node) {
		if (rootVisited) {
			build(node);
		}
	}

	private void visitGoingUp(MutableTreeNode node) {

	}

	private void build(MutableTreeNode node) {
		// TODO if label which can be URI is not promising use string label
		SimpleModule module = new SimpleModule();
		if (node.posTag.matches("WD(.)*")) {
			// if node is WD* skip

		} else if (node.children.size() == 0 && node.posTag.matches("ADD|NN(.)*")) {
			// if node a leaf and posTag is ADD or NN*
			for (int j = variableNumber; j >= 0; --j) {
				WhereClause wc = new WhereClause("?a" + j, "IS", node.label);
				module.addStatement(wc);
			}
		} else {
			// adding where clauses
			WhereClause wc = new WhereClause("?a" + (variableNumber - 1), RDF.type.getURI(), node.label);
			module.addStatement(wc);
			// Replacement rule to form different BGPs
			// (?a(i) = ?a(j) or ?a(j) != ?a(i))
			// for i!=j and i,j = [0,|modules|]
			for (int i = variableNumber; i >= 0; --i) {
				for (int j = variableNumber; j >= 0; --j) {
					if (i != j) {
						wc = new WhereClause("?a" + i, node.label, "?a" + j);
						module.addStatement(wc);
						wc = new WhereClause("?a" + i, node.label, "?a" + j);
						module.addStatement(wc);
					}
				}
			}
			variableNumber++;
		}
		if (module.statementList.size() > 0) {
			list.add(module);
		}
	}

}
