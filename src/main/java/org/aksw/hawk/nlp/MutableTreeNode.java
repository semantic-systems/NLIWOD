package org.aksw.hawk.nlp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class MutableTreeNode implements Comparable<MutableTreeNode>, Serializable {
	private static final long serialVersionUID = 3684161169564127853L;
	public String label;
	public String posTag;
	public List<MutableTreeNode> children = new ArrayList<>();
	public MutableTreeNode parent;
	public String depLabel;
	public int nodeNumber;
	private boolean used = false;
	private ArrayList<ResourceImpl> annotations = new ArrayList<>();
	public String lemma;

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
		return label + ": " + posTag;
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


	public void addAnnotation(ResourceImpl resourceImpl) {
		if (annotations == null) {
			annotations = new ArrayList<>();
		}
		annotations.add(resourceImpl);
	}

	public List<ResourceImpl> getAnnotations() {
		if (annotations == null) {
			return new ArrayList<>();
		} else {
			return annotations;
		}
	}
}
