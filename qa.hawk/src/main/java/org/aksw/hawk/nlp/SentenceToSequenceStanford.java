package org.aksw.hawk.nlp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

//TODO make this class independently of other classes callable
//TODO write unit test
public class SentenceToSequenceStanford {
	private static String language = AbstractReader.LANG_EN;
	static AbstractTokenizer tokenizer = NLPGetter.getTokenizer(language);

	// combine noun phrases
	public static void combineSequences(final HAWKQuestion q) {
		// run pos-tagging

		Logger log = LoggerFactory.getLogger(SentenceToSequenceStanford.class);
		MutableTree tree = q.getTree();

		String sentence = q.getLanguageToQuestion().get("en");
		List<String> tokens = tokenizer.getTokens(sentence);
		Map<String, String> label2pos = generatePOSTags(q);

		// TODO: Stanford Tree is already POS-tagged and has dependency
		// recognition, not necessary to do it again!

		// run phrase combination
		List<String> subsequence = Lists.newArrayList();
		for (int tcounter = 0; tcounter < tokens.size(); tcounter++) {
			String token = tokens.get(tcounter);
			String pos = label2pos.get(token);
			String nextPos = tcounter + 1 == tokens.size() ? null : label2pos.get(tokens.get(tcounter + 1));
			String lastPos = tcounter == 0 ? null : label2pos.get(tokens.get(tcounter - 1));

			// look for start "RB|JJ|NN(.)*"
			if (subsequence.isEmpty() && null != pos && pos.matches("CD|JJ|NN(.)*|RB(.)*")) {
				subsequence.add(token);
			}
			// split "of the" or "of all" or "against" via pos_i=IN and
			// pos_i+1=DT
			else if (!subsequence.isEmpty() && null != pos && tcounter + 1 < tokens.size() && null != nextPos && pos.matches("IN") && !token.matches("of") && nextPos.matches("(W)?DT|NNP(S)?")) {
				if (subsequence.size() > 1) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			}
			// do not combine NNS and NNPS but combine "stage name",
			// "British Prime minister"
			else if (!subsequence.isEmpty() && null != pos && null != lastPos && lastPos.matches("NNS") && pos.matches("NNP(S)?")) {
				if (subsequence.size() > 2) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			}
			// finish via VB* or IN -> null or IN -> DT or WDT (now a that or
			// which follows)
			else if (!subsequence.isEmpty() && !lastPos.matches("JJ|HYPH")
			        && (null == pos || pos.matches("VB(.)*|\\.|WDT") || (pos.matches("IN") && nextPos == null) || (pos.matches("IN") && nextPos.matches("DT")))) {
				// more than one token, so summarizing makes sense
				if (subsequence.size() > 1) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			}
			// continue via "NN(.)*|RB|CD|CC|JJ|DT|IN|PRP|HYPH"
			else if (!subsequence.isEmpty() && null != pos && pos.matches("NN(.)*|RB|CD|CC|JJ|DT|IN|PRP|HYPH|VBN")) {
				subsequence.add(token);
			} else {
				subsequence = Lists.newArrayList();
			}
		}
		log.debug("combined Phrases...");
		if (!q.getLanguageToNamedEntites().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNamedEntites().get("en"));
			log.debug(sentence);
		}
		if (!q.getLanguageToNounPhrases().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNounPhrases().get("en"));
			log.debug(sentence);
		}
		// DEPTree tree=new DEPTree();
		// tree=NLPGetter.toDEPTree(tokenizer.getTokens(sentence));
		// Enter changes into tree
		// q.setTree(MutableTreeFactory.depToMutableDEP(tree));
		// resolveCompoundNouns(q.getTree(),
		// q.getLanguageToNounPhrases().get("en"));
		q.setTree(tree);
	}

	private static Map<String, String> generatePOSTags(final HAWKQuestion q) {

		Map<String, String> label2pos = Maps.newHashMap();
		// TODO this is horribly wrong, the same label CAN have different pos if
		// the label occurs twice in question
		// Map<String, String> label2pos = Maps.newTreeMap();
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(q.getTree().getRoot());
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			label2pos.put(tmp.label, tmp.posTag);
			for (MutableTreeNode child : tmp.getChildren()) {
				stack.push(child);
			}
		}
		return label2pos;
	}

	private static void transformTree(final List<String> subsequence, final HAWKQuestion q) {
		String combinedNN = Joiner.on(" ").join(subsequence);
		String combinedURI = "http://aksw.org/combinedNN/" + Joiner.on("_").join(subsequence);
		MutableTree tree = q.getTree();
		Entity tmpEntity = new Entity();
		tmpEntity.label = combinedNN;
		tmpEntity.uris.add(new ResourceImpl(combinedURI));

		List<Entity> nounphrases = q.getLanguageToNounPhrases().get("en");
		if (null == nounphrases) {
			nounphrases = Lists.newArrayList();
		}
		nounphrases.add(tmpEntity);

		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(tree.getRoot());
		List<MutableTreeNode> removables = new ArrayList<>();
		while (!stack.isEmpty()) {

			MutableTreeNode thisNode = stack.pop();
			String label = thisNode.label;
			for (String s : subsequence) {
				if (label.contains(s)) {
					// thisNode.label =
					// Joiner.on("
					// ").join(label.replace("http://aksw.org/combinedNN/",
					// "").split("_"));
					thisNode.label = combinedNN;
					thisNode.posTag = "CombinedNN";
					if (!thisNode.equals(tree.getRoot())) {
						if (thisNode.label == thisNode.parent.label) {
							removables.add(thisNode);
						}
					}
				}
			}

			for (MutableTreeNode child : thisNode.getChildren()) {
				stack.push(child);
			}

		}
		for (MutableTreeNode m : removables) {
			Log.info(SentenceToSequenceStanford.class, "Removing node " + m.nodeNumber + ": " + m.toString());
			tree.remove(m);
			// TODO: Fix Node Numbers after removing them.
		}
		q.setTree(tree);
	}

	// private static void resolveCompoundNouns(MutableTree tree, List<Entity>
	// list) {
	//
	// Stack<MutableTreeNode> stack = new Stack<MutableTreeNode>();
	// stack.push(tree.getRoot());
	// while (!stack.isEmpty()) {
	//
	// MutableTreeNode thisNode = stack.pop();
	// String label = thisNode.label;
	// if (label.contains("aksw.org")) {
	// thisNode.label =
	// Joiner.on(" ").join(label.replace("http://aksw.org/combinedNN/",
	// "").split("_"));
	// thisNode.posTag = "CombinedNN";
	// }
	// for (MutableTreeNode child : thisNode.getChildren()) {
	// stack.push(child);
	// }
	// }
	//
	// }

	private static String replaceLabelsByIdentifiedURIs(String sentence, final List<Entity> list) {
		for (Entity entity : list) {
			if (!entity.label.equals("")) {
				// " " inserted so punctuation gets separated correctly from
				// URIs
				sentence = sentence.replace(entity.label, entity.uris.get(0).getURI() + " ").trim();
			}
		}
		return sentence;
	}

}
