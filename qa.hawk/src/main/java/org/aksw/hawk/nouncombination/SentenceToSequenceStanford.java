package org.aksw.hawk.nouncombination;

import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.hawk.nlp.SentenceToSequence;

class SentenceToSequenceStanford extends ANounCombiner {
	private static SentenceToSequenceStanford instance;

	private SentenceToSequenceStanford() {

	}

	static SentenceToSequenceStanford getInstance() {
		if (instance == null) {
			instance = new SentenceToSequenceStanford();
		}
		return instance;
	}

	@Override
	protected void combineNouns(final HAWKQuestion q) {
		MutableTree tree = q.getTree();
		List<String> tokens = new Vector<>();
		for (MutableTreeNode it : tree.getAllNodesInSentenceOrder()) {
			tokens.add(it.getLabel());
		}
		Map<String, String> label2pos = tree.getPOSTags();
		SentenceToSequence.runPhraseCombination(q, tokens, label2pos);

		processTree(q);
	}

}
