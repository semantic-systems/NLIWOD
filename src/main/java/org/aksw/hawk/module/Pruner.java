package org.aksw.hawk.module;

import java.util.Iterator;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTree;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pruner {
	Logger log = LoggerFactory.getLogger(Pruner.class);

	public MutableTree prune(Question q) {
		applyPunctuationRules(q);
		applyDeterminantRules(q);
		/* interrogative rules last else each interrogative word has at least
		 two children, which can't be handled yet by the removal*/
		applyInterrogativeRules(q);

		return q.tree;
	}

	private void applyInterrogativeRules(Question q) {
		MutableTreeNode root = q.tree.getRoot();

		if (root.label.equals("Give")) {
			for (Iterator<MutableTreeNode> it = root.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode next = it.next();
				if (next.label.equals("me")) {
					it.remove();
					q.tree.remove(root);
				}
			}
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

	private void applyPunctuationRules(Question q) {
		inorderRemovalPunctuations(q.tree.getRoot(), q.tree);

	}

	private boolean inorderRemovalPunctuations(MutableTreeNode node, MutableTree tree) {
		if (node.posTag.equals(".")) {
			tree.remove(node);
			return true;
		} else {
			for (Iterator<MutableTreeNode> it = node.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode child = it.next();
				if (inorderRemovalPunctuations(child, tree)) {
					it = node.getChildren().iterator();
				}
			}
			return false;
		}

	}

}
