package org.aksw.hawk.nlp;

import java.util.List;

import org.aksw.hawk.index.DBAbstractsIndex;

import com.clearnlp.tokenization.EnglishTokenizer;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class SentenceToSequence {
	DBAbstractsIndex index = new DBAbstractsIndex();

	public SentenceToSequence() {

	}

	private List<String> sequence(String sentence) {
		EnglishTokenizer tok = new EnglishTokenizer();

		List<String> list = tok.getTokens(sentence);
		// build subsequences out of token
		List<String> subsequences = Lists.newArrayList();
		for (int windowSize = list.size(); windowSize > 1; windowSize--) {
			for (int sub = 0; sub + windowSize < list.size(); sub++) {
				subsequences.add(Joiner.on(" ").join(list.subList(sub, sub + windowSize)));
			}
		}
		return subsequences;
	}

	private List<String> index(String token) {
		return index.listAbstractsContaining(token);
	}

	public static void main(String args[]) {
		// String sentence =
		// "Who succeeded the pope that reigned only 33 days?";
		String sentence = "only 33 days?";
		SentenceToSequence sts = new SentenceToSequence();

		List<String> list = sts.sequence(sentence);

		for (String item : list) {
			System.out.println(item);
			for (String abstracts : sts.index(item)) {
				System.out.println(abstracts);
			}
		}
	}

}
