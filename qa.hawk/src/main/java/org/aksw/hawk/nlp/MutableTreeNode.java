package org.aksw.hawk.nlp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

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
	private int depth;

	/**
	 * The position of label word in sentence.
	 */
	private int labelPosition;

	public MutableTreeNode() {
	}

	public MutableTreeNode hardcopy(final MutableTreeNode parent) {
		MutableTreeNode node = new MutableTreeNode(label, posTag, depLabel, parent, nodeNumber, lemma, labelPosition);
		node.annotations = new ArrayList<>(annotations);
		node.used = used;
		for (MutableTreeNode it : children) {
			node.children.add(it.hardcopy(node));
		}
		return node;
	}

	/**
	 * Returns all Nodes beneath this node.(Subtree)
	 *
	 * @return
	 */
	public List<MutableTreeNode> subNodes() {
		List<MutableTreeNode> out = new ArrayList<>();
		out.add(this);
		if (getChildren().isEmpty()) {
			return out;
		}
		for (MutableTreeNode it : getChildren()) {
			out.addAll(it.subNodes());
		}
		return out;
	}

	public MutableTreeNode(final String label, final String posTag, final String depLabel, final MutableTreeNode parent, final int i, final String lemma) {
		this.label = label;
		this.posTag = posTag;
		this.parent = parent;
		this.depLabel = depLabel;
		this.nodeNumber = i;
		this.lemma = lemma;
		if (parent != null) {
			this.depth = parent.getDepth() + 1;
		} else {
			this.depth = 0;
		}
	}

	public MutableTreeNode(final String label, final String posTag, final String depLabel, final MutableTreeNode parent, final int i, final String lemma, final int labelPosition) {
		this.label = label;
		this.posTag = posTag;
		this.parent = parent;
		this.depLabel = depLabel;
		this.nodeNumber = i;
		this.lemma = lemma;
		if (parent != null) {
			this.depth = parent.getDepth() + 1;
		} else {
			this.depth = 0;
		}

		this.setLabelPosition(labelPosition);
	}

	public void addChild(final MutableTreeNode newNode) {
		children.add(newNode);
	}

	public List<MutableTreeNode> getChildren() {
		return children;
	}

	@Override
	public String toString() {
		return label + ":" + "\t posTag: " + posTag + "\t lemma: " + lemma + "\t depLabel: " + depLabel;
	}

	@Override
	public int compareTo(final MutableTreeNode o) {
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

	public void addAnnotation(final String resourceImpl) {
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

	public String getDepLabel() {
		return depLabel;
	}

	public String getLabel() {
		return label;
	}

	public String getPosTag() {
		return posTag;
	}

	public String getLemma() {
		return lemma;
	}

	public void setDepLabel(final String depLabel) {
		this.depLabel = depLabel;
	}

	public void setLabel(final String label) {
		this.label = label;
	}

	public void setPosTag(final String posTag) {
		this.posTag = posTag;
	}

	public void setLemma(final String lemma) {
		this.lemma = lemma;
	}

	public int getLabelPosition() {
		return labelPosition;
	}

	public void setLabelPosition(final int labelPosition) {
		this.labelPosition = labelPosition;
	}

	public int getDepth() {
		return depth;
	}

}
