package org.aksw.hawk.nlp;

import java.util.Iterator;
import java.util.LinkedList;

public class MutableTreeNodeIterator implements Iterator<MutableTreeNode> {

	private MutableTreeNode root = null;
	private MutableTreeNode current = null;
	private LinkedList<Integer> path = new LinkedList<Integer>();
	private int nodeTier = -1;
	private boolean hasNext = false;

	@Override
	public boolean hasNext() {
		if (hasNext) {
			return current != null;
		} else {
			current = next();
			hasNext = true;
			if (current != null) {

				return true;
			}
		}
		return false;
	}

	public MutableTreeNodeIterator(MutableTreeNode root) {
		this.root = root;
	}

	@Override
	public MutableTreeNode next() {
		if (hasNext) {
			hasNext = false;
			return current;
		}
		hasNext = false;
		if (path.isEmpty()) {
			// INITIAL
			path.add(0);
			current = root;
			nodeTier = 0;
			return current;
		} else if (nodeTier == path.size() - 1) {
			// CASE: Node is finished, the childs not even began with

			if (current.getChildren().size() > 0) {
				nodeTier++;
				path.add(0);
				current = current.getChildren().get(0);
				return current;
			} else {
				// CASE get back
				nodeTier--;
				current = current.parent;
				// path.remove();
				return next();
			}
		} else {
			// CASE we have a last path and are back at the parent node
			int lastChild = path.removeLast();
			if (lastChild + 1 < current.getChildren().size()) {
				nodeTier++;
				current = current.getChildren().get(lastChild + 1);
				path.add(lastChild + 1);
				return current;
			}

			else {
				// CASE get back
				nodeTier--;
				if (nodeTier < 0) {
					return null;
				}
				// path.remove();
				current = current.parent;
				return next();
			}
		}
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub

	}

	public int getTier() {
		return nodeTier;
	}

	public void reset() {
		current = null;
		path = new LinkedList<Integer>();
		nodeTier = -1;
		hasNext = false;
	}

}