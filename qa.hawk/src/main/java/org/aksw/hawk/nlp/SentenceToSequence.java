package org.aksw.hawk.nlp;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SentenceToSequence {

	static Logger log = LoggerFactory.getLogger(SentenceToSequence.class);

	public static void runPhraseCombination(final HAWKQuestion q, final List<String> tokens, final Map<String, String> label2pos) {
		// run phrase combination
		int tokenOffset = 0;
		int wordCounter = 0;
		List<String> subsequence = Lists.newArrayList();
		for (int tcounter = 0; tcounter < tokens.size(); tcounter++) {
			wordCounter += tokens.get(tcounter).split(" ").length;
			String token = tokens.get(tcounter);
			String pos = label2pos.get(token);
			String nextPos = (tcounter + 1) == tokens.size() ? null : label2pos.get(tokens.get(tcounter + 1));
			String lastPos = tcounter == 0 ? null : label2pos.get(tokens.get(tcounter - 1));

			if (subsequence.isEmpty()) {
				tokenOffset = wordCounter;
			}

			// look for start "RB|JJ|NN(.)*"
			if (subsequence.isEmpty() && (null != pos) && pos.matches("CD|JJ|NN(.)*|RB(.)*")) {
				subsequence.add(token);
			}
			// split "of the" or "of all" or "against" via pos_i=IN and
			// pos_i+1=DT
			else if (!subsequence.isEmpty() && (null != pos) && ((tcounter + 1) < tokens.size()) && (null != nextPos) && pos.matches("IN") && !token.matches("of")
			        && nextPos.matches("(W)?DT|NNP(S)?")) {
				if (subsequence.size() > 1) {
					transformTree(subsequence, q, tokenOffset);
				}
				subsequence = Lists.newArrayList();
			}
			// do not combine NNS and NNPS but combine "stage name",
			// "British Prime minister"
			else if (!subsequence.isEmpty() && (null != pos) && (null != lastPos) && lastPos.matches("NNS") && pos.matches("NNP(S)?")) {
				if (subsequence.size() > 2) {
					transformTree(subsequence, q, tokenOffset);
				}
				subsequence = Lists.newArrayList();
			}
			// finish via VB* or IN -> null or IN -> DT or WDT (now a that or
			// which follows)
			else if (!subsequence.isEmpty() && !lastPos.matches("JJ|HYPH")
			        && ((null == pos) || pos.matches("VB(.)*|\\.|WDT") || (pos.matches("IN") && (nextPos == null)) || (pos.matches("IN") && nextPos.matches("DT")))) {
				// more than one token, so summarizing makes sense
				if (subsequence.size() > 1) {
					transformTree(subsequence, q, tokenOffset);
				}
				subsequence = Lists.newArrayList();
			}
			// continue via "NN(.)*|RB|CD|CC|JJ|DT|IN|PRP|HYPH"
			else if (!subsequence.isEmpty() && (null != pos) && pos.matches("NN(.)*|RB|CD|CC|JJ|DT|IN|PRP|HYPH|VBN")) {
				subsequence.add(token);
			} else {
				subsequence = Lists.newArrayList();
			}
		}
		log.debug(q.getLanguageToNounPhrases().toString());
	}

	public static void transformTree(final List<String> subsequence, final HAWKQuestion q, final int subsequenceStartOffset) {
		String combinedNN = Joiner.on(" ").join(subsequence);
		String combinedURI = "http://aksw.org/combinedNN/" + Joiner.on("_").join(subsequence);

		Entity tmpEntity = new Entity();
		tmpEntity.setOffset(subsequenceStartOffset);

		tmpEntity.setLabel(combinedNN);

		tmpEntity.getUris().add(new ResourceImpl(combinedURI));

		List<Entity> nounphrases = q.getLanguageToNounPhrases().get("en");
		if (null == nounphrases) {
			nounphrases = Lists.newArrayList();
		}
		nounphrases.add(tmpEntity);
		q.getLanguageToNounPhrases().put("en", nounphrases);

	}

	public static void resolveCompoundNouns(final MutableTree tree, final List<Entity> list) {

		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(tree.getRoot());
		while (!stack.isEmpty()) {

			MutableTreeNode thisNode = stack.pop();
			String label = thisNode.label;
			if (label.contains("aksw.org")) {
				thisNode.label = Joiner.on(" ").join(label.replace("http://aksw.org/combinedNN/", "").split("_"));
				thisNode.posTag = "CombinedNN";
			}
			for (MutableTreeNode child : thisNode.getChildren()) {
				stack.push(child);
			}
		}

	}

}
