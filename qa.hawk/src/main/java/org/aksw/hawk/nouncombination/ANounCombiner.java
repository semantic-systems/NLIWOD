package org.aksw.hawk.nouncombination;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.ext.com.google.common.base.Joiner;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import com.google.common.collect.Lists;

abstract class ANounCombiner {
	/**
	 * This sets compound nouns as Entity in question. No changes to
	 * MutableTree.
	 *
	 *
	 * <pre>
	 * <strong>Note:</strong> This expects expects HAWKquestion already to be dependency
	 *  parsed, e.g. a MutableTree with annotated Nodes is set.
	 *
	 * Use {@link #combineNounsOnNew(HAWKQuestion)} to get a new processed tree,
	 * without affecting HAWKQuestion
	 *
	 * Use {@link #processTree(HAWKQuestion)} to prune all found CombinedNNs
	 * in MutbaleTree directly in HAWKQuestion
	 * </pre>
	 *
	 * @param q HAwkquestion to process
	 */
	protected abstract void combineNouns(HAWKQuestion q);

	/**
	 * Compound nouns are spread over multiple Nodes in MutableTree. This will
	 * be reduced to one Node, which has "CombinedNN" as POS tag, as label all
	 * labels from this noun combined.
	 *
	 * {@link HAWKQuestion#getLanguageToNounPhrases()} is used to find Nodes to
	 * combine. Be sure to set combinedNouns there using
	 * {@link #combineNouns(HAWKQuestion)} beforehand.
	 *
	 * @param q HAWKQuestion to process
	 */

	protected static void processTree(final HAWKQuestion q) {
		MutableTree tree = q.getTree();

		Map<String, List<Entity>> entities = q.getLanguageToNounPhrases();
		List<Entity> entityList = entities.get("en");
		if (entityList == null) {
			return;
		}

		for (Entity it : entityList) {

			String combinedNN = Joiner.on(" ").join(it.getUris().get(0).getURI().replace("http://aksw.org/combinedNN/", "").split("_"));
			List<String> subsequence = Arrays.asList(combinedNN.split(" "));

			Stack<MutableTreeNode> stack = new Stack<>();
			stack.push(tree.getRoot());
			Set<MutableTreeNode> removables = new HashSet<>();
			while (!stack.isEmpty()) {

				MutableTreeNode thisNode = stack.pop();
				String label = thisNode.label;
				for (String iterator : subsequence) {

					if (label.equals(iterator) && (thisNode.getLabelPosition() >= it.getOffset()) && (thisNode.getLabelPosition() <= (it.getOffset() + subsequence.size()))) {
						thisNode.label = combinedNN;
						thisNode.posTag = "CombinedNN";
						if (!thisNode.equals(tree.getRoot())) {
							if (thisNode.parent.getLabel() == combinedNN) {
								removables.add(thisNode);
							}
							/**
							 * Workaround for a systemic problem: when the
							 * distance in dependency graph between labels of
							 * one combined noun is greater than one, this will
							 * still remove corresponding nodes. But distance
							 * souldnt be greater than one, right?
							 *
							 */
							for (MutableTreeNode pathNode : tree.getPathToRoot(thisNode)) {
								if (pathNode.getLabel() == combinedNN) {
									removables.add(thisNode);
								}
							}

							/**
							 * This is a workaround for a systemic problem:
							 * sometimes combined nouns are found, where the
							 * corresponding nodes are not in a child-parent
							 * relation, so we check them too for nodes to
							 * eliminate. But should be child-parent in the
							 * first place, so by now noun combination or
							 * dependency tree parsing messed up.
							 */
							List<MutableTreeNode> sameDepth = tree.getAllNodesWithDepth(thisNode.getDepth());
							sameDepth.remove(thisNode);
							for (MutableTreeNode sameDepthNode : sameDepth) {
								if (sameDepthNode.getLabel() == combinedNN) {
									removables.add(thisNode);
								}
							}
						}
					}
				}

				for (MutableTreeNode child : thisNode.getChildren()) {
					stack.push(child);
				}

			}
			for (MutableTreeNode m : removables) {
				Log.debug(ANounCombiner.class, "Removing node " + m.nodeNumber + ": " + m.toString());
				tree.remove(m);

			}
			tree.updateNodeNumbers();
		}
	}

	protected static void setEntity(final List<String> subsequence, final HAWKQuestion q, final int subsequenceStartOffset) {
		String combinedNN = Joiner.on(" ").join(subsequence);
		String combinedURI = "http://aksw.org/combinedNN/" + Joiner.on("_").join(subsequence);

		Entity tmpEntity = new Entity();
		tmpEntity.setLabel(combinedNN);
		tmpEntity.setOffset(subsequenceStartOffset);
		tmpEntity.getUris().add(new ResourceImpl(combinedURI));

		List<Entity> nounphrases = q.getLanguageToNounPhrases().get("en");
		if (null == nounphrases) {
			nounphrases = Lists.newArrayList();
			q.getLanguageToNounPhrases().put("en", nounphrases);
		}
		nounphrases.add(tmpEntity);
	}

}
