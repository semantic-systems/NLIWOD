package org.aksw.hawk.nlp;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTree;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pruner {
	Logger log = LoggerFactory.getLogger(Pruner.class);

	public MutableTree prune(Question q) {
		log.debug(q.tree.toString());
		removalRules(q);
		applyDeterminantRules(q);
		applyPDTRules(q);
		applyINRules(q);
		applyAuxPassRules(q);
		// applyJJRule(q);// TODO JJ rule is very vague
		// applyCombineNNRule(q);
		// applyWDTRule(q);
		/*
		 * interrogative rules last else each interrogative word has at least
		 * two children, which can't be handled yet by the removal
		 */
		applyInterrogativeRules(q);
		sortTree(q.tree);
		log.info(q.tree.toString());
		return q.tree;
	}

	private void sortTree(MutableTree tree) {
		Queue<MutableTreeNode> queue = new LinkedList<MutableTreeNode>();
		queue.add(tree.getRoot());
		while (!queue.isEmpty()) {
			MutableTreeNode tmp = queue.poll();
			Collections.sort(tmp.getChildren());
			queue.addAll(tmp.getChildren());
		}

	}

	private void applyAuxPassRules(Question q) {
		inorderRemovalAuxPass(q.tree.getRoot(), q.tree);

	}

	private boolean inorderRemovalAuxPass(MutableTreeNode node, MutableTree tree) {
		if (node.depLabel.equals("auxpass")) {
			tree.remove(node);
			return true;
		} else {
			for (Iterator<MutableTreeNode> it = node.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode child = it.next();
				if (inorderRemovalAuxPass(child, tree)) {
					it = node.getChildren().iterator();
				}
			}
			return false;
		}
	}

	// pre determiner: all, both
	private void applyPDTRules(Question q) {
		inorderRemovalPDT(q.tree.getRoot(), q.tree);

	}

	private boolean inorderRemovalPDT(MutableTreeNode node, MutableTree tree) {
		if (node.posTag.equals("PDT")) {
			tree.remove(node);
			return true;
		} else {
			for (Iterator<MutableTreeNode> it = node.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode child = it.next();
				if (inorderRemovalPDT(child, tree)) {
					it = node.getChildren().iterator();
				}
			}
			return false;
		}
	}

	// removes BY and IN
	private void applyINRules(Question q) {
		inorderRemovalIN(q.tree.getRoot(), q.tree);

	}

	private boolean inorderRemovalIN(MutableTreeNode node, MutableTree tree) {
		if (node.posTag.equals("IN")) {
			tree.remove(node);
			return true;
		} else {
			for (Iterator<MutableTreeNode> it = node.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode child = it.next();
				if (inorderRemovalIN(child, tree)) {
					it = node.getChildren().iterator();
				}
			}
			return false;
		}
	}

	private void applyInterrogativeRules(Question q) {
		MutableTreeNode root = q.tree.getRoot();
		// GIVE ME will be deleted
		if (root.label.equals("Give")) {
			for (Iterator<MutableTreeNode> it = root.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode next = it.next();
				if (next.label.equals("me")) {
					it.remove();
					q.tree.remove(root);
				}
			}
		}
		// LIST will be deleted
		if (root.label.equals("List")) {
			q.tree.remove(root);
		}

	}

	private void applyDeterminantRules(Question q) {
		inorderRemovalDeterminats(q.tree.getRoot(), q.tree);

	}

	private boolean inorderRemovalDeterminats(MutableTreeNode node, MutableTree tree) {
		if (node.posTag.equals("DT")) {
			tree.remove(node);
			return true;
		} else {
			for (Iterator<MutableTreeNode> it = node.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode child = it.next();
				if (inorderRemovalDeterminats(child, tree)) {
					it = node.getChildren().iterator();
				}
			}
			return false;
		}
	}

	/**
	 * removes punctuations (.) and wh- words(WDT|WP|WRB)
	 * 
	 * @param q
	 */
	private void removalRules(Question q) {
		MutableTreeNode root = q.tree.getRoot();
		inorderRemoval(root, q.tree);

	}

	private boolean inorderRemoval(MutableTreeNode node, MutableTree tree) {
		if (node.posTag.matches(".|WDT|WP|WRB")) {
			tree.remove(node);
			return true;
		} else {
			for (Iterator<MutableTreeNode> it = node.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode child = it.next();
				if (inorderRemoval(child, tree)) {
					it = node.getChildren().iterator();
				}
			}
			return false;
		}

	}

}
