package org.aksw.hawk.nlp;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;

public class TreeTransformer {
	Logger log = LoggerFactory.getLogger(TreeTransformer.class);

	static int i = 0;

	public MutableTree DEPtoMutableDEP(DEPTree tmp) {
		MutableTree tree = new MutableTree();
		i = 0;
		addNodeRecursivly(tree, tree.head, tmp.getFirstRoot());

		return tree;
	}

	private void addNodeRecursivly(MutableTree tree, MutableTreeNode parent, DEPNode depNode) {

		MutableTreeNode newParent = null;
		if (parent == null) {
			newParent = new MutableTreeNode(depNode.form, depNode.pos, depNode.getLabel(), null, i, depNode.lemma);
			tree.head = newParent;
		} else {
			newParent = new MutableTreeNode(depNode.form, depNode.pos, depNode.getLabel(), parent, i, depNode.lemma);
			parent.addChild(newParent);
		}
		for (DEPNode tmpChilds : depNode.getDependentNodeList()) {
			i++;
			addNodeRecursivly(tree, newParent, tmpChilds);
		}
	}

	public static MutableTree semanticGraphToMutableTree(SemanticGraph graph) {
		i = 0;
		MutableTree tree = new MutableTree();
		MutableTreeNode mutableRoot;

		IndexedWord graphRoot = graph.getFirstRoot();

		mutableRoot = new MutableTreeNode(graphRoot.word(), graphRoot.tag(), "root", null, i++, graphRoot.lemma());
		tree.head = mutableRoot;

		convertGraphStanford(mutableRoot, graphRoot, graph);

		return tree;
	}

	private static void convertGraphStanford(MutableTreeNode parentMutableNode, IndexedWord parentGraphWord, SemanticGraph graph) {

		if (!graph.hasChildren(parentGraphWord)) {
			return;
		}

		Set<IndexedWord> childrenWords = graph.getChildren(parentGraphWord);

		for (IndexedWord child : childrenWords) {

			SemanticGraphEdge edge = graph.getEdge(parentGraphWord, child);
			String depLabel = edge.getRelation().getShortName();
			MutableTreeNode childMutableNode = new MutableTreeNode(child.word(), child.tag(), depLabel, parentMutableNode, i++, child.lemma());
			parentMutableNode.addChild(childMutableNode);
			convertGraphStanford(childMutableNode, child, graph);
		}

	}
}
