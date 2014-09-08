package org.aksw.hawk.nlp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

public class TreeTransformer {
	Logger log = LoggerFactory.getLogger(TreeTransformer.class);

	static int i = 0;

	public MutableTree DEPtoMutableDEP(DEPTree tmp) {
		MutableTree tree = new MutableTree();
		 i = 0;
		addNodeRecursivly(tree, tree.head, tmp.getFirstRoot());

		return tree;
	}

	private void addNodeRecursivly(MutableTree tree, MutableTreeNode parent, DEPNode depNode) {

		MutableTreeNode newParent = null;
		if (parent == null) {
			newParent = new MutableTreeNode(depNode.form, depNode.pos, depNode.getLabel(), null, i, depNode.lemma);
			tree.head = newParent;
		} else {
			newParent = new MutableTreeNode(depNode.form, depNode.pos, depNode.getLabel(), parent, i,depNode.lemma);
			parent.addChild(newParent);
		}
		for (DEPNode tmpChilds : depNode.getDependentNodeList()) {
			i++;
			addNodeRecursivly(tree, newParent, tmpChilds);
		}
	}
}
