package org.aksw.hawk.nlp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

//TODO refactor class
public class MutableTreeNode implements Comparable<MutableTreeNode>, Serializable {
	private static final long serialVersionUID = 3684161169564127853L;
	public List<MutableTreeNode> children = new ArrayList<>();
	private boolean used = false;
	public MutableTreeNode parent;
	public int nodeNumber;
	public String depLabel;
	public String label;
	public String posTag;
	public String lemma;
	private List<String> annotations = Lists.newArrayList();

	public MutableTreeNode() {
	}

	public MutableTreeNode(String label, String posTag, String depLabel, MutableTreeNode parent, int i, String lemma) {
		this.label = label;
		this.posTag = posTag;
		this.parent = parent;
		this.depLabel = depLabel;
		this.nodeNumber = i;
		this.lemma = lemma;
	}

	public void addChild(MutableTreeNode newNode) {
		children.add(newNode);
	}

	public List<MutableTreeNode> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return label + ":" + "\t posTag: " + posTag + "\t lemma: " + lemma;
	}

	@Override
	public int compareTo(MutableTreeNode o) {
		if (this.nodeNumber > o.nodeNumber) {
			return 1;
		} else {
			return -1;
		}
	}

	public void isUsed() {
		used = true;
	}

	public boolean used() {
		return used;
	}

	public void addAnnotation(String resourceImpl) {
		if (annotations == null) {
			annotations = new ArrayList<>();
		}
		if (!resourceImpl.isEmpty()) {
			annotations.add(resourceImpl);
		}
	}

	public List<String> getAnnotations() {
		if (annotations == null) {
			return new ArrayList<>();
		} else {
			return annotations;
		}
	}
}
