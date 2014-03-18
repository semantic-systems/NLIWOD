package org.aksw.hawk.nlp.posTree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

public class TreeTransformer {
	Logger log = LoggerFactory.getLogger(TreeTransformer.class);

	public MutableTree DEPtoMutableDEP(DEPTree tmp) {
		MutableTree tree = new MutableTree();
		addNodeRecursivly(tree, tree.head, tmp.getFirstRoot());

		return tree;
	}

	private void addNodeRecursivly(MutableTree tree, MutableTreeNode parent, DEPNode depNode) {

		MutableTreeNode newParent = null;
		if (parent == null) {
			newParent = new MutableTreeNode(depNode.form, depNode.pos, null);
			tree.head = newParent;
		} else {
			newParent = new MutableTreeNode(depNode.form, depNode.pos, parent);
			parent.addChild(newParent);
		}
		for (DEPNode tmpChilds : depNode.getDependentNodeList()) {
			addNodeRecursivly(tree, newParent, tmpChilds);
		}

	}
}
