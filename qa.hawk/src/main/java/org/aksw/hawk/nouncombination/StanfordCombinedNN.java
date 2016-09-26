package org.aksw.hawk.nouncombination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.MutableTreeNode;

/**
 *
 * This combines Nodes with dependency label "compound" with its parent.
 *
 * @author Jonathan
 *
 */
class StanfordCombinedNN extends ANounCombiner {
	private static StanfordCombinedNN instance;

	private StanfordCombinedNN() {
	}

	static StanfordCombinedNN getInstance() {
		if (instance == null) {
			instance = new StanfordCombinedNN();
		}
		return instance;
	}

	@Override
	protected void combineNouns(final HAWKQuestion q) {
		convert(q.getTree().getRoot(), q);
		processTree(q);
	}

	private void convert(final MutableTreeNode parentNode, final HAWKQuestion q) {
		List<MutableTreeNode> children = new ArrayList<>(parentNode.getChildren());
		List<MutableTreeNode> removeMe = new ArrayList<>();

		/**
		 * Don´t combine if its a named entity.
		 */
		for (MutableTreeNode child : children) {
			if (child.label.toLowerCase().contains("http://dbpedia.org/resource/")) {
				removeMe.add(child);
			}
		}
		/**
		 * Filter for compound relations.
		 */
		for (MutableTreeNode it : children) {
			if (!it.depLabel.toLowerCase().equals("compound")) {
				removeMe.add(it);
			}
		}

		/**
		 * check if there is something to process.
		 */
		children.removeAll(removeMe);
		if (!children.isEmpty()) {
			/**
			 * If we have children with compound, its in compound relation with
			 * parent. Therefore we need to add parent to list for processing.
			 */
			children.add(parentNode);
			/**
			 * Sort by word position, to get compound word string.
			 */
			Collections.sort(children, new Comparator<MutableTreeNode>() {
				@Override
				public int compare(final MutableTreeNode one, final MutableTreeNode two) {
					return one.getLabelPosition() - two.getLabelPosition();
				}
			});

			/**
			 * This sets the CombinedNN as Entity in Question
			 */
			ArrayList<String> orderlyWords = new ArrayList<>();
			for (MutableTreeNode compoundChild : children) {
				orderlyWords.add(compoundChild.label);
			}
			setEntity(orderlyWords, q, children.get(0).getLabelPosition());
			/**
			 * Merge all compound-children with parent
			 */
			// parentNode.setPosTag("CombinedNN");
			// parentNode.setLabel(Joiner.on(" ").join(orderlyWords));
			// parentNode.lemma = "#url#";
			// parentNode.getChildren().removeAll(children);
			// /**
			// * Get the children of eliminated Nodes and add then to children
			// of
			// * parents e.g. deal with grandchildren
			// */
			// children.remove(parentNode);
			// for (MutableTreeNode it : children) {
			// parentNode.getChildren().addAll(it.getChildren());
			// }

		}
		if (parentNode.getChildren().isEmpty()) {
			return;
		}
		for (MutableTreeNode it : parentNode.getChildren()) {
			convert(it, q);
		}

	}

}
