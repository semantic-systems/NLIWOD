package org.aksw.hawk.module;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTree;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;

public class Pruner {

	public MutableTree prune(Question q) {
		applyInterrogativeRules(q);
		applyPunctuationRules(q);
		applyDeterminantRules(q);
		return q.tree;
	}

	private void applyInterrogativeRules(Question q) {
		MutableTreeNode firstRoot = q.tree.getRoot();

		if (firstRoot.label.equals("Give")) {
			for (MutableTreeNode node : firstRoot.getChildren()) {
				if (node.label.equals("me")) {
					System.out.println(q.tree.remove(node));
					System.out.println(q.tree.remove(firstRoot));
				}
			}
		}
		System.out.println(q.tree.toStringSRL());

	}

	private void applyDeterminantRules(Question q) {
		// TODO Auto-generated method stub

	}

	private void applyPunctuationRules(Question q) {
		// TODO Auto-generated method stub

	}

}
