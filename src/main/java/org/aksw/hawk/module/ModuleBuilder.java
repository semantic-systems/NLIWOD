package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class ModuleBuilder {
	Logger log = LoggerFactory.getLogger(ModuleBuilder.class);

	public List<Module> build(MutableTreeNode mutableTreeNode, List<Module> list, Question q) {
		if (list == null) {
			list = new ArrayList<>();
		}
		// 5. Find projection variable
		List<Module> findProjectionVariable = findProjectionVariable(q);
		if (findProjectionVariable == null) {
			return null;
		}
		list.addAll(findProjectionVariable);

		// add constraint modules by traversing the tree
		List<Module> findConstraintVariable = findConstraintVariable(q);
		list.addAll(findConstraintVariable);
		log.debug("\n" + Joiner.on("\n").join(list));
		return list;
	}

	private List<Module> findConstraintVariable(Question q) {
		List<Module> tmp = new ArrayList<>();
		// traverse sub arguments depth first search
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(q.tree.getRoot());
		while (!stack.isEmpty()) {
			log.debug(stack.peek().toString());
			MutableTreeNode pop = stack.pop();
			// if a not yet used argument is found in the parse tree use it to
			// add to the query
			if (!pop.used()) {
				int numberOfChildren = pop.getChildren().size();
				if (numberOfChildren == 0) {

				} else if (numberOfChildren == 1) {
					if (pop.getChildren().get(0).getChildren().size() == 0) {
						MutableTreeNode constraintPredicate = pop;
						MutableTreeNode constraintObject = pop.getChildren().get(0);
						// mark the subnode as already used
						constraintObject.isUsed();
						// construct constraint module
						Module projConstraint = new ConstraintModule(constraintPredicate, constraintObject);
						tmp.add(projConstraint);
					} else {
						log.error("Cannot build constraint variable at question " + q.id + " since this case is not implemented");
					}
				} else if (numberOfChildren == 2) {
					log.error("Cannot build constraint variable at question " + q.id + " since this case is not implemented");
				} else {
					log.error("Cannot build constraint variable at question " + q.id + " since this case is not implemented");
				}
			}
			// depth first search
			List<MutableTreeNode> children = pop.getChildren();
			Collections.reverse(children);
			for (MutableTreeNode child : children) {
				stack.push(child);
			}

		}

		return tmp;
	}

	private List<Module> findProjectionVariable(Question q) {
		List<Module> tmp = new ArrayList<>();
		// construct first left most path bottom up
		List<MutableTreeNode> projectionVariable = findProjectionVariableR(q, q.tree.getRoot(), null);
		Collections.reverse(projectionVariable);

		// --Hard coded dictionary--//
		if (projectionVariable.get(0).label.toLowerCase().equals("which")) {
			// mark each node in a "Which" query as used
			for (MutableTreeNode n : projectionVariable) {
				n.isUsed();
			}
			// look for the next noun in this path
			projectionVariable.remove(0);
			MutableTreeNode type = null;
			for (MutableTreeNode el : projectionVariable) {
				if (el.posTag.equals("NNS") || el.posTag.equals("NN")) {
					type = el;
					break;
				}
			}
			// if no noun on path, by now it is an error
			if (type == null) {
				log.error("NO TYPE VARIABLE FOUND: " + q.id);
				return null;
			}
			// else the root noun is used as projection constroint e.g. In which
			// X is Y Z
			// becomes ?uri rdf:type ?X . ?X ?Y ?Z.
			else {
				Module proj = new ProjectionModule(type);
				tmp.add(proj);
				projectionVariable.remove(0);
				Module projConstraint = new ProjectionConstraintModule(projectionVariable.get(0));
				tmp.add(projConstraint);
			}

		} else {
			log.error("NO RULE APPLIED FOR THIS QUESTION: " + q.id);
			return null;
		}
		return tmp;
	}

	/**
	 * go down the left most path to extract the question word and its first
	 * argument predicate
	 */
	private List<MutableTreeNode> findProjectionVariableR(Question q, MutableTreeNode mutableTreeNode, List<MutableTreeNode> list) {
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(mutableTreeNode);
		List<MutableTreeNode> children = mutableTreeNode.getChildren();
		if (!children.isEmpty()) {
			return findProjectionVariableR(q, children.get(0), list);
		}
		return list;
	}
}
