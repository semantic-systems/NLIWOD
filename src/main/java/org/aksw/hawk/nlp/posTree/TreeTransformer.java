package org.aksw.hawk.nlp.posTree;

import org.aksw.hawk.nlp.TreeTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

public class TreeTransformer {
	Logger log = LoggerFactory.getLogger(getClass());

	public MutableTree DEPtoMutableDEP(DEPTree tmp) {

		MutableTree tree = new MutableTree();

		addNodeRecursivly(tree.head, tmp.getFirstRoot());

		log.debug(TreeTraversal.inorderTraversal(tree.head, 0, new StringBuilder()));
		return tree;
	}

	private void addNodeRecursivly(MutableTreeNode parent, DEPNode depNode) {

		MutableTreeNode newNode = new MutableTreeNode();
		newNode.label = depNode.form;
		newNode.posTag = depNode.pos;
		if (parent == null) {
			parent = newNode;
		} else {
			parent.addChild(newNode);
		}
		for (DEPNode tmpChilds : depNode.getDependentNodeList()) {
			addNodeRecursivly(newNode, tmpChilds);
		}

	}
}
