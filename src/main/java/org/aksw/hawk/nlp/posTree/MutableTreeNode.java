package org.aksw.hawk.nlp.posTree;

import java.util.ArrayList;
import java.util.List;

public class MutableTreeNode {

	public String label;
	public String posTag;
	public List<MutableTreeNode> children = new ArrayList<>();
	public MutableTreeNode parent;
	public String depLabel;

	public MutableTreeNode(String label, String posTag, String depLabel, MutableTreeNode parent) {
		this.label = label;
		this.posTag = posTag;
		this.parent = parent;
		this.depLabel = depLabel;
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
