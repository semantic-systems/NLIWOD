package org.aksw.hawk.nlp;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

// TODO make this class independently of other classes callable
// TODO write unit test
public class SentenceToSequence {
	private static String language = AbstractReader.LANG_EN;
	static AbstractTokenizer tokenizer = NLPGetter.getTokenizer(language);
	static Logger log = LoggerFactory.getLogger(SentenceToSequence.class);

	// combine noun phrases
	public static void combineSequences(final HAWKQuestion q) {
		// run pos-tagging
		String sentence = q.getLanguageToQuestion().get("en");

		List<String> tokens = tokenizer.getTokens(sentence);

		System.out.println(tokens.toString());
		Map<String, String> label2pos = generatePOSTags(q);

		runPhraseCombination(q, tokens, label2pos);
		// log.debug(q.languageToNounPhrases.get("en"));
	}

	public static Map<String, String> generatePOSTags(final HAWKQuestion q) {
		ParseTree parse = new ParseTree();
		DEPTree tree = parse.process(q);

		// TODO this is horribly wrong, the same label CAN have different pos if
		// the label occurs twice in question
		Map<String, String> label2pos = Maps.newHashMap();
		Stack<DEPNode> stack = new Stack<>();
		stack.push(tree.getFirstRoot());
		while (!stack.isEmpty()) {
			DEPNode tmp = stack.pop();
			label2pos.put(tmp.form, tmp.pos);
			for (DEPNode child : tmp.getDependentNodeList()) {
				stack.push(child);
			}
		}

		return label2pos;

	}

	public static void runPhraseCombination(final HAWKQuestion q, final List<String> tokens, final Map<String, String> label2pos) {
		// run phrase combination
		List<String> subsequence = Lists.newArrayList();
		for (int tcounter = 0; tcounter < tokens.size(); tcounter++) {
			String token = tokens.get(tcounter);
			String pos = label2pos.get(token);
			String nextPos = (tcounter + 1) == tokens.size() ? null : label2pos.get(tokens.get(tcounter + 1));
			String lastPos = tcounter == 0 ? null : label2pos.get(tokens.get(tcounter - 1));

			// look for start "RB|JJ|NN(.)*"
			if (subsequence.isEmpty() && (null != pos) && pos.matches("CD|JJ|NN(.)*|RB(.)*")) {
				subsequence.add(token);
			}
			// split "of the" or "of all" or "against" via pos_i=IN and
			// pos_i+1=DT
			else if (!subsequence.isEmpty() && (null != pos) && ((tcounter + 1) < tokens.size()) && (null != nextPos) && pos.matches("IN") && !token.matches("of")
			        && nextPos.matches("(W)?DT|NNP(S)?")) {
				if (subsequence.size() > 1) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			}
			// do not combine NNS and NNPS but combine "stage name",
			// "British Prime minister"
			else if (!subsequence.isEmpty() && (null != pos) && (null != lastPos) && lastPos.matches("NNS") && pos.matches("NNP(S)?")) {
				if (subsequence.size() > 2) {
					transformTree(subsequence, q);
				}
				subsequence = Lists.newArrayList();
			}
			// finish via VB* or IN -> null or IN -> DT or WDT (now a that or
			// which follows)
			else if (!subsequence.isEmpty() && !lastPos.matches("JJ|HYPH")
			        && ((null == pos) || pos.matches("VB(.)*|\\.|WDT") || (pos.matches("IN") && (nextPos == null)) || (pos.matches("IN") && nextPos.matches("DT")))) {
				// more than one token, so summarizing makes sense
				if (subsequence.size() > 1) {
					transformTree(subsequence, q);
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
		// log.debug(q.languageToNounPhrases.get("en"));
	}

	public static void transformTree(final List<String> subsequence, final HAWKQuestion q) {
		String combinedNN = Joiner.on(" ").join(subsequence);
		String combinedURI = "http://aksw.org/combinedNN/" + Joiner.on("_").join(subsequence);

		Entity tmpEntity = new Entity();
		tmpEntity.label = combinedNN;
		tmpEntity.uris.add(new ResourceImpl(combinedURI));

		List<Entity> nounphrases = q.getLanguageToNounPhrases().get("en");
		if (null == nounphrases) {
			nounphrases = Lists.newArrayList();
		}
		nounphrases.add(tmpEntity);
		q.getLanguageToNounPhrases().put("en", nounphrases);

	}

	private static void resolveCompoundNouns(final MutableTree tree, final List<Entity> list) {

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

	// TODO Christian: transform to unit test
	public static void main(final String args[]) {
		HAWKQuestion q = new HAWKQuestion();
		StanfordNLPConnector conn = new StanfordNLPConnector();

		q.getLanguageToQuestion().put("en", "Who was vice-president under the president who authorized atomic weapons against Japan during World War II?");
		conn.parseTree(q, null);
		SentenceToSequence.combineSequences(q);
		resolveCompoundNouns(q.getTree(), q.getLanguageToNounPhrases().get("en"));
		System.out.println(q.getLanguageToNounPhrases().get("en"));
		System.out.println(q.getTree().toString());
		System.exit(0);

		q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "Who plays Phileas Fogg in the adaptation of Around the World in 80 Days directed by Buzz Kulik?");
		conn.parseTree(q, null);
		SentenceToSequence.combineSequences(q);
		resolveCompoundNouns(q.getTree(), q.getLanguageToNounPhrases().get("en"));
		System.out.println(q.getLanguageToNounPhrases().get("en"));

		q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "Which recipients of the Victoria Cross died in the Battle of Arnhem?");
		conn.parseTree(q, null);
		SentenceToSequence.combineSequences(q);
		resolveCompoundNouns(q.getTree(), q.getLanguageToNounPhrases().get("en"));
		System.out.println("LANGUAGETONOUNPHRASES");
		System.out.println(q.getLanguageToNounPhrases().get("en"));
	}
}
