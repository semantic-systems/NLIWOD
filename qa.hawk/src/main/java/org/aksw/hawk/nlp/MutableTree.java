package org.aksw.hawk.nlp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MutableTree implements Serializable {
	private static final long serialVersionUID = 1286195006804443794L;
	static Logger log = LoggerFactory.getLogger(MutableTree.class);
	private MutableTreeNode root = null;

	public MutableTreeNode getRoot() {
		return root;
	}

	public void setRoot(final MutableTreeNode root) {
		this.root = root;
	}

	public boolean remove(final MutableTreeNode target) {

		if (!getAllNodes().contains(target)) {
			return false;
		}

		if (target.equals(root)) {
			if (root.children.size() == 1) {
				root = root.children.get(0);
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
		return TreeTraversal.inorderTraversal(root, 0, null);
	}

	/**
	 * Returns a hardcopy of this MutableTree.
	 *
	 * @return new MutableTree
	 */
	public MutableTree hardcopy() {
		MutableTree tree = new MutableTree();
		tree.root = this.root.hardcopy(null);
		return tree;
	}

	/**
	 * Returns all Nodes in the same order they occur in underlying sentence.
	 *
	 */
	public List<MutableTreeNode> getAllNodesInSentenceOrder() {
		List<MutableTreeNode> inOrder = new Vector<>();

		inOrder = getAllNodes();
		if (inOrder.isEmpty()) {
			return inOrder;
		}
		Collections.sort(inOrder, new Comparator<MutableTreeNode>() {
			@Override
			public int compare(final MutableTreeNode one, final MutableTreeNode two) {
				return one.getLabelPosition() - two.getLabelPosition();
			}
		});

		return inOrder;
	}

	/**
	 * Returns all Nodes in underlying sentence.
	 *
	 */
	public List<MutableTreeNode> getAllNodes() {
		List<MutableTreeNode> allNodes = new Vector<>();
		if (root == null) {
			return allNodes;
		}
		return root.subNodes();
	}

	/**
	 *
	 * @return All tokens mapped to corresponding POS tags
	 */
	public Map<String, String> getPOSTags() {
		Map<String, String> tokenToPOS = new HashMap<>();
		for (MutableTreeNode node : getAllNodes()) {
			tokenToPOS.put(node.getLabel(), node.getPosTag());
		}
		return tokenToPOS;
	}

	public void updateNodeNumbers() {
		List<MutableTreeNode> orderlyNodes = getAllNodes();
		Collections.sort(orderlyNodes);

		for (int i = 0; i < orderlyNodes.size(); i++) {
			orderlyNodes.get(i).nodeNumber = i;
		}

	}

	public List<MutableTreeNode> getAllNodesWithDepth(final int depth) {
		List<MutableTreeNode> sameDepth = new ArrayList<>();
		for (MutableTreeNode it : getAllNodes()) {
			if (it.getDepth() == depth) {
				sameDepth.add(it);
			}
		}
		return sameDepth;
	}

	public List<MutableTreeNode> getPathToRoot(final MutableTreeNode node) {
		ArrayList<MutableTreeNode> path = new ArrayList<>();
		MutableTreeNode current = node;
		do {
			current = current.parent;
			path.add(current);

		} while (current != root);
		return path;
	}

}
