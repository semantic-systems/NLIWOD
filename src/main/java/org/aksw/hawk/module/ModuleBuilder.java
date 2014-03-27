package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		if(findProjectionVariable== null){
			return null;
		}
		list.addAll(findProjectionVariable);

		log.debug(Joiner.on("\n").join(list));
		return list;
	}

	private List<Module> findProjectionVariable(Question q) {
		List<Module> tmp = new ArrayList<>();
		List<MutableTreeNode> projectionVariable = findProjectionVariableR(q, q.tree.getRoot(), null);
		Collections.reverse(projectionVariable);

		// --Hard coded dictionary--//
		if (projectionVariable.get(0).label.toLowerCase().equals("which")) {
			log.debug("\t==>" + Joiner.on("   ").join(projectionVariable));
			projectionVariable.remove(0);
			MutableTreeNode type = null;
			for (MutableTreeNode el : projectionVariable) {
				if (el.posTag.equals("NNS") || el.posTag.equals("NN")) {
					type = el;
					break;
				}
			}
			if (type == null) {
				log.error("NO TYPE VARIABLE FOUND: " + q.id);
				return null;
			} else {
				Module module = new ProjectionModule(type);
				tmp.add(module);
			}
			log.debug("\t==>" + Joiner.on("   ").join(projectionVariable));

		} else {
			log.error("NO RULE APPLIED FOR THIS QUESTION: " +q.id);
			return null;
		}
		return tmp;
	}

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
