package org.aksw.hawk.module;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.index.DBAbstractsIndex;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class Fulltexter {
	public static Logger log = LoggerFactory.getLogger(Fulltexter.class);

	public HashMap<String, Set<RDFNode>> fulltext(Question q) {
		HashMap<String, Set<RDFNode>> map = Maps.newHashMap();
		DBAbstractsIndex index = new DBAbstractsIndex();
		Set<RDFNode> set = Sets.newHashSet();
		Stack<MutableTreeNode> stack = new Stack<MutableTreeNode>();
		stack.push(q.tree.getRoot());
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			if (tmp.posTag.equals("CombinedNN") || tmp.posTag.matches("NN(.)*")) {
				List<String> listAbstractsContaining = index.listAbstractsContaining(tmp.label);
				log.debug(tmp + " : " + listAbstractsContaining.size());
				for (String resourceURL : listAbstractsContaining) {
					set.add(new ResourceImpl(resourceURL));
				}
			}
			for (MutableTreeNode child : tmp.getChildren()) {
				stack.push(child);
			}
		}
		map.put(q.languageToQuestion.get("en"), set);
		index.close();
		return map;
	}
}
