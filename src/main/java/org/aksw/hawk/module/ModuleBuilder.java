package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPNode;

public class ModuleBuilder {
	Logger log = LoggerFactory.getLogger(ModuleBuilder.class);

	public List<Module> build(DEPNode rootPredicate, List<Module> list, Question q) {
		if (list == null) {
			list = new ArrayList<>();
		}
		/*
		 * traverse tree by recursive build a module for each traverse
		 * predicate-argument arc
		 */
		for (DEPArc arc : rootPredicate.getDependents()) {
			DEPNode node = arc.getNode();
			log.debug(rootPredicate.lemma + " -> " + node.lemma);
			/*
			 * for each arc build a type of, predicate and possibly predicate
			 * subject statement
			 */
			Module module = new Module(rootPredicate, node,q);
			list.add(module);
			build(node, list,q);

		}
		return list;
	}
}
