package org.aksw.hawk.nlp;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.aksw.hawk.datastructures.HAWKQuestion;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;
import com.google.common.collect.Maps;

public class SentenceToSequenceOpenNLP {
	private static String language = AbstractReader.LANG_EN;
	static AbstractTokenizer tokenizer = NLPGetter.getTokenizer(language);

	// combine noun phrases
	public static void combineSequences(final HAWKQuestion q) {
		// run pos-tagging
		String sentence = q.getLanguageToQuestion().get("en");

		List<String> tokens = tokenizer.getTokens(sentence);

		System.out.println(tokens.toString());
		Map<String, String> label2pos = generatePOSTags(q);

		SentenceToSequence.runPhraseCombination(q, tokens, label2pos);
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
}
