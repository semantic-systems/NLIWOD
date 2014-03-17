package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModuleBuilder {
	Logger log = LoggerFactory.getLogger(ModuleBuilder.class);

	public List<Module> build(MutableTreeNode mutableTreeNode, List<Module> list, Question q) {
		if (list == null) {
			list = new ArrayList<>();
		}
		/*
		 * traverse tree by recursive build a module for each traverse
		 * predicate-argument arc
		 */
		for (MutableTreeNode node : mutableTreeNode.getChildren()) {
			/*
			 * for each arc build a type of, predicate and possibly predicate
			 * subject statement
			 */
			log.debug(mutableTreeNode.label + " -> " + node.label);
			Module module = new Module(mutableTreeNode, node,q);
			list.add(module);
			build(node, list,q);

		}
		return list;
	}
}
