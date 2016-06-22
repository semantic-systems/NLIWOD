package org.aksw.hawk.cache;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.hawk.nlp.ParseTree;
import org.aksw.hawk.util.JSONStatusBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;

//FIXME interface and test
public class CachedParseTreeClearnlp {
	private Logger log = LoggerFactory.getLogger(CachedParseTreeClearnlp.class);
	private ParseTree parseTree;
	private boolean useCache = false;
	private int i;

	public MutableTree process(HAWKQuestion q) {
		if (isStored(q) != null && useCache) {
			return StorageHelper.readFromFileSavely(isStored(q));
		} else {
			log.info("Tree not cached.");
			if (parseTree == null) {
				parseTree = new ParseTree();
			}
			DEPTree t = parseTree.process(q);
			MutableTree mutableTree = depToMutableDEP(t);
			System.out.println(mutableTree.toString());

			q.setTree_full(JSONStatusBuilder.treeToJSON(mutableTree));
			store(q, mutableTree);
			return mutableTree;
		}
	}

	/**
	 * stores a dependency tree to a X.ser file and writes the file to
	 * dependency tree mapping to an index file, TextInNodes is not serialized
	 * in this version
	 * 
	 * @param q
	 *            Question
	 * @param DEPtoMutableDEP
	 *            mutable parse tree
	 */
	private void store(HAWKQuestion q, MutableTree DEPtoMutableDEP) {
		String question = q.getLanguageToQuestion().get("en");
		int hash = question.hashCode();
		String serializedFileName = "cache/" + hash + ".tree";
		// log.error(DEPtoMutableDEP.toString());
		StorageHelper.storeToFileSavely(DEPtoMutableDEP, serializedFileName);

	}

	/**
	 * returns mapping from a certain question to the file where the dependency
	 * tree is stored
	 * 
	 * @param q
	 *            Question
	 * @return filename File with dependency tree in serialized form without
	 *         TextInNode attribute
	 */
	private String isStored(HAWKQuestion q) {
		String question = q.getLanguageToQuestion().get("en");
		int hash = question.hashCode();
		String serializedFileName = "cache/" + hash + ".tree";

		File ser = new File(serializedFileName);
		if (ser.exists()) {
			return serializedFileName;
		} else {
			log.debug("Question not stored in CachedParseTree");
			return null;
		}
	}

	private MutableTree depToMutableDEP(DEPTree tmp) {
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

	public void test() {

		HAWKQuestion q = new HAWKQuestion();
		Map<String, String> languageToQuestion = new HashMap<String, String>();
		languageToQuestion.put("en", "Which anti-apartheid activist was born in Mvezo?");
		q.setLanguageToQuestion(languageToQuestion);
		process(q);

	}

	public static void main(String args[]) {
		new CachedParseTreeClearnlp().test();
	}
}
