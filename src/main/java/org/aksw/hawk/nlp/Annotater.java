package org.aksw.hawk.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.index.DBAbstractsIndex;
import org.aksw.hawk.index.DBOIndex;
import org.aksw.hawk.index.IndexDBO_classes;
import org.aksw.hawk.index.IndexDBO_properties;
import org.aksw.hawk.nlp.posTree.MutableTree;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class Annotater {
	Logger log = LoggerFactory.getLogger(Annotater.class);
	IndexDBO_classes classesIndex = new IndexDBO_classes();
	IndexDBO_properties propertiesIndex = new IndexDBO_properties();
	DBOIndex dboIndex = new DBOIndex();
	DBAbstractsIndex index = new DBAbstractsIndex();

	public void annotateTree(Question q) {
		MutableTree tree = q.tree;
		annotateProjectionLeftTree(tree);
		annotateVerbs(tree);
		annotateNouns(tree);
	}

	/**
	 * "Named entities are noun phrases and are usually modelled as resources,
	 * thus a lexical entry is built comprising a syntactic noun phrase
	 * representation together with a corresponding semantic representation
	 * containing a resource slot." citation by Unger et al. tbsl
	 */
	/**
	 * "Nouns are often referring to classes, while sometimes to properties,
	 * thus two lexical entries are built { one containing a semantic
	 * representation with a class slot and one containing a semantic
	 * representation with a property slot." citation by Unger et al. tbsl
	 * 
	 * @param tree
	 */
	private void annotateNouns(MutableTree tree) {
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(tree.getRoot());
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			String label = tmp.label;
			String posTag = tmp.posTag;
			if (posTag.matches("NN(.)*") && tmp.getAnnotations().isEmpty()) {
				// ArrayList<String> search = propertiesIndex.search(label);
				// if (search.isEmpty() && tmp.lemma != null) {
				// search = propertiesIndex.search(tmp.lemma);
				// } else if (search.isEmpty()) {
				// search = dboIndex.search(label);
				// }
				// for (String uri : search) {
				// tmp.addAnnotation(new ResourceImpl(uri));
				// }
				// log.debug(Joiner.on(", ").join(tmp.getAnnotations()));
				// System.out.println(label);
			} else if (posTag.matches("CombinedNN") && tmp.getAnnotations().isEmpty()) {
//		TODO		System.out.println(label + "->" + index.listAbstractsContaining(label).size());
			}
			for (MutableTreeNode child : tmp.getChildren()) {
				stack.push(child);
			}
		}
	}

	/**
	 * "Verbs most often refer to properties, thus a lexical entry with a
	 * property slot is built. However, in some cases, the verb does not
	 * contribute anything to the query structure (like have in Which cities
	 * have more than 2 million inhabitants?), thus an additional entry is
	 * built, that does not contain a property slot corresponding to the verb
	 * but assumes that the property slot is contributed by a noun (inhabitants
	 * in this case)." citation by Unger et al. tbsl
	 * 
	 * @param tree
	 */
	private void annotateVerbs(MutableTree tree) {
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(tree.getRoot());
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			String label = tmp.label;
			String posTag = tmp.posTag;
			if (posTag.matches("VB(.)*")) {
				ArrayList<String> search = propertiesIndex.search(label);
				if (search.isEmpty() && tmp.lemma != null) {
					search = propertiesIndex.search(tmp.lemma);
				} else if (search.isEmpty()) {
					search = dboIndex.search(label);
				}
				for (String uri : search) {
					tmp.addAnnotation(new ResourceImpl(uri));
				}
				log.debug(Joiner.on(", ").join(tmp.getAnnotations()));
			}
			for (MutableTreeNode child : tmp.getChildren()) {
				stack.push(child);
			}
		}
	}

	/**
	 * this method annotates the left-most child of the root and uses the inline
	 * commented heuristics to annotate the tree
	 * 
	 * @param tree
	 */
	private void annotateProjectionLeftTree(MutableTree tree) {
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(tree.getRoot().getChildren().get(0));

		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			String label = tmp.label;
			String posTag = tmp.posTag;

			// only one projection variable node
			if (tmp.children.size() == 0) {
				if (posTag.matches("WRB|WP")) {
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
					if (posTag.matches("NNS")) {
						// TODO improve lemmatization. e.g., birds->bird
						if (tmp.lemma != null)
							label = tmp.lemma;
					}
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
					// TODO ask actress also in dbo owl
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
					log.error("Strange case that never should happen: " + posTag);
				}
			}
			break;
		}
	}
}
