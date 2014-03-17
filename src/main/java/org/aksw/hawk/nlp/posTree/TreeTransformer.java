package org.aksw.hawk.nlp.posTree;

import org.aksw.hawk.nlp.TreeTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

public class TreeTransformer {
	Logger log = LoggerFactory.getLogger(TreeTransformer.class);

	public MutableTree DEPtoMutableDEP(DEPTree tmp) {
		MutableTree tree = new MutableTree();
		addNodeRecursivly(tree, tree.head, tmp.getFirstRoot());

		String tmpString = TreeTraversal.inorderTraversal(tree.head, 0, null);
		log.debug(tmpString);
		return tree;
	}

	private void addNodeRecursivly(MutableTree tree, MutableTreeNode parent, DEPNode depNode) {

		MutableTreeNode newNode = new MutableTreeNode();
		newNode.label = depNode.form;
		newNode.posTag = depNode.pos;
		if (parent == null) {
			tree.head = newNode;
		} else {
			parent.addChild(newNode);
		}
		for (DEPNode tmpChilds : depNode.getDependentNodeList()) {
			addNodeRecursivly(tree, newNode, tmpChilds);
		}

	}
}
