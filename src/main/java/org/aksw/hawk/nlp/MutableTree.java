package org.aksw.hawk.nlp;

import java.io.Serializable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutableTree implements Serializable {
	private static final long serialVersionUID = 1286195006804443794L;
	static Logger log = LoggerFactory.getLogger(MutableTree.class);
	public MutableTreeNode head = null;

	public MutableTreeNode getRoot() {
		return head;
	}

	public boolean remove(MutableTreeNode target) {

		if (target.equals(head)) {
			if (head.children.size() == 1) {
				head = head.children.get(0);
				return true;
			} else {
				// more than one child on to be removed root
				log.error("More than one child on to be removed root. Need to rebalance tree or something.");
				return false;
			}
		} else {
			List<MutableTreeNode> children = target.children;
			MutableTreeNode parent = target.parent;
			List<MutableTreeNode> parentsChildren = parent.children;
			parentsChildren.addAll(children);
			for (MutableTreeNode grandchild : children) {
				grandchild.parent = parent;
			}
			parentsChildren.remove(target);
			return true;
		}
	}

	@Override
	public String toString() {
		return TreeTraversal.inorderTraversal(head, 0, null);
	}

}
