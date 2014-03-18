package org.aksw.hawk.nlp.posTree;

import java.util.ArrayList;
import java.util.List;

public class MutableTreeNode {

	public String label;
	public String posTag;
	public List<MutableTreeNode> children = new ArrayList<>();
	public MutableTreeNode parent;

	public MutableTreeNode(String label, String posTag, MutableTreeNode parent) {
		this.label = label;
		this.posTag = posTag;
		this.parent = parent;
	}

	public void addChild(MutableTreeNode newNode) {
		children.add(newNode);
	}

	public List<MutableTreeNode> getChildren() {
		return children;
	}

	public String toString() {
		return label + ": " + posTag;
	}
}
