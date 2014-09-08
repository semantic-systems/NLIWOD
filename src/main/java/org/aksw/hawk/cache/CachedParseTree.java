package org.aksw.hawk.cache;

import java.io.File;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.ParseTree;
import org.aksw.hawk.nlp.TreeTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPTree;

public class CachedParseTree {
	private Logger log = LoggerFactory.getLogger(CachedParseTree.class);
	private ParseTree parseTree;
	private TreeTransformer treeTransform;

	public CachedParseTree() {
		treeTransform = new TreeTransformer();
	}

	public MutableTree process(Question q) {
		if (isStored(q) != null) {
			return StorageHelper.readFromFileSavely(isStored(q));
		} else {
			if (parseTree == null) {
				parseTree = new ParseTree();
			}
			DEPTree t = parseTree.process(q);
			MutableTree DEPtoMutableDEP = treeTransform.DEPtoMutableDEP(t);
			store(q, DEPtoMutableDEP);
			return DEPtoMutableDEP;
		}
	}

	/**
	 * stores a dependency tree to a <X>.ser file and writes the file to
	 * dependency tree mapping to an index file, TextInNodes is not serialized
	 * in this version TODO
	 * 
	 * @param q
	 *            Question
	 * @param DEPtoMutableDEP
	 *            mutable parse tree
	 */
	private void store(Question q, MutableTree DEPtoMutableDEP) {
		String question = q.languageToQuestion.get("en");
		int hash = question.hashCode();
		String serializedFileName = "cache/trees/" + hash + ".tree";
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
	 *         TextInNode attribute TODO
	 */
	private String isStored(Question q) {
		String question = q.languageToQuestion.get("en");
		int hash = question.hashCode();
		String serializedFileName = "cache/trees/" + hash + ".tree";

		File ser = new File(serializedFileName);
		if (ser.exists()) {
			return serializedFileName;
		} else {
			log.debug("Question not stored in CachedParseTree");
			return null;
		}
	}

}
