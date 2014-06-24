package org.aksw.hawk.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.index.DBAbstractsIndex;
import org.aksw.hawk.index.IndexDBO_classes;
import org.aksw.hawk.index.IndexDBO_properties;
import org.aksw.hawk.nlp.posTree.MutableTree;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class Annotater {
	Logger log = LoggerFactory.getLogger(Annotater.class);
	IndexDBO_classes classesIndex = new IndexDBO_classes();
	IndexDBO_properties propertiesIndex = new IndexDBO_properties();
	DBAbstractsIndex index = new DBAbstractsIndex();

	public void annotateTree(Question q) {
		Stack<MutableTreeNode> stack = new Stack<>();
		MutableTree tree = q.tree;
		stack.push(tree.getRoot().getChildren().get(0));
		log.debug(q.tree.toString());
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			String label = tmp.label;
			String posTag = tmp.posTag;

			// only one projection variable node
			if (tmp.children.size() == 0) {
				if (posTag.equals("W(.)*")) {
					// gives only hints towards the type of projection variable
					if (label.equals("Where")) {
						tmp.addAnnotation(new ResourceImpl("http://dbpedia.org/ontology/Place"));
					} else if (label.equals("Who")) {
						tmp.addAnnotation(new ResourceImpl("http://dbpedia.org/ontology/Agent"));
					}
				} else if (posTag.equals("CombinedNN")) {
					// full text lookup
					List<String> uris = index.listAbstractsContaining(label);
					for (String resourceURL : uris) {
						tmp.addAnnotation(new ResourceImpl(resourceURL));
					}
				} else if (posTag.matches("NN(.)*")) {
					// DBO look up
					if (classesIndex.search(label).size() > 0) {
						ArrayList<String> uris = classesIndex.search(label);
						for (String resourceURL : uris) {
							tmp.addAnnotation(new ResourceImpl(resourceURL));
						}
					} else {
						log.error("Strange case that never should happen");
					}
				} else if (posTag.equals("ADD")) {
					// strange case
					// since entities should not be the question word type
					log.error("Strange case that never should happen: " + posTag);
				}
			} else {
				// imperative word queries like "List .." or "Give me.." do have
				// parse trees where the root is a NN(.)*
				if (posTag.matches("NN(.)*")) {
					if (classesIndex.search(label).size() > 0) {
						ArrayList<String> uris = classesIndex.search(label);
						for (String resourceURL : uris) {
							tmp.addAnnotation(new ResourceImpl(resourceURL));
						}
					} else {
						// full text lookup
						DBAbstractsIndex index = new DBAbstractsIndex();
						List<String> uris = index.listAbstractsContaining(label);
						for (String resourceURL : uris) {
							tmp.addAnnotation(new ResourceImpl(resourceURL));
						}
					}
				} else {

				}

				// if X (of or P) Y which is a path in the left tree do
				// X as pred and Y as Entity (full-text or index)

			}
			break;
			// // for each VB* ask property index
			// if (posTag.matches("VB(.)*")) {
			// // System.out.println(label + " \t\t\t= " +
			// // Joiner.on(", ").join(propertiesIndex.search(label)));
			// }
			// // for each NN* ask class index
			// else if (posTag.matches("NN(.)*")) {
			// // System.out.println(label + " \t\t\t= " +
			// // Joiner.on(", ").join(classesIndex.search(label)));
			// }
			// for (MutableTreeNode child : tmp.getChildren()) {
			// stack.push(child);
			// }
			//
			// // set.add(new ResourceImpl(resourceURL));

		}
	}
}
