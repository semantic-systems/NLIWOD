package org.aksw.hawk.cache;

import java.io.File;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.ParseTree;
import org.aksw.hawk.nlp.TreeTransformer;
import org.aksw.hawk.util.JSONStatusBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPTree;

//FIXME interface and test
public class CachedParseTreeClearnlp implements CachedParseTree {
	private Logger log = LoggerFactory.getLogger(CachedParseTreeClearnlp.class);
	private ParseTree parseTree;
	private TreeTransformer treeTransform;
	private boolean useCache = false;

	public CachedParseTreeClearnlp() {
		treeTransform = new TreeTransformer();
	}

	public MutableTree process(HAWKQuestion q) {
		if (isStored(q) != null && useCache) {
			return StorageHelper.readFromFileSavely(isStored(q));
		} else {
			log.info("Tree not cached.");
			if (parseTree == null) {
				parseTree = new ParseTree();
			}
			DEPTree t = parseTree.process(q);
			MutableTree DEPtoMutableDEP = treeTransform.DEPtoMutableDEP(t);
			System.out.println(DEPtoMutableDEP.toString());

			q.setTree_full(JSONStatusBuilder.treeToJSON(DEPtoMutableDEP));
			store(q, DEPtoMutableDEP);
			return DEPtoMutableDEP;
		}
	}

	/**
	 * stores a dependency tree to a <X>.ser file and writes the file to
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

}
