package org.aksw.hawk.module;

import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class ModuleBuilder {
	Logger log = LoggerFactory.getLogger(ModuleBuilder.class);

	public List<Module> build(MutableTreeNode mutableTreeNode, List<Module> list, Question q) {
		// 5. Find projection variable
		List<Module> modules = Lists.newArrayList();

		// traverse tree down, to a WDT, WP, WP$, WRB posTag and use its parent
		// node to build:
		// ?uri a parent
		// ?uri parent ?y
		// ?y parent ?uri
		// TODO if using direct label which can be URI is not that promising use
		// also the label

		// afterwards build for each node
		// ?y a node
		// ?y node ?x
		// ?x node ?y mit der Ersetzungregel (?uri = ?y or ?uri != ?y)

		// wenn man auf einen ADD knoten als blatt oder einen NN* trifft
		// ?x = node mit der Ersetzungregel(?x =?y oder ?x != ?y)

		log.debug("\n" + Joiner.on("\n").join(list));
		return list;
	}

}
