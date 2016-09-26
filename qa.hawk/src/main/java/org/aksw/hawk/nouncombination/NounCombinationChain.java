package org.aksw.hawk.nouncombination;

import java.util.ArrayList;
import java.util.Arrays;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;

public class NounCombinationChain {
	private ArrayList<ANounCombiner> chain;

	/**
	 * Creates a chain with all given Combiners. The order of attributes you
	 * pass on are relevant.
	 *
	 * there is nothing special about firstCombiner. The idea is that you have
	 * to pass on at least one attribute.
	 *
	 * @param combiners The combiners you want to use, in given order.
	 */
	public NounCombinationChain(final NounCombiners firstCombiner, final NounCombiners... followingCombiners) {
		chain = new ArrayList<>();
		ArrayList<NounCombiners> combinerEnums = new ArrayList<>();
		combinerEnums.add(firstCombiner);
		combinerEnums.addAll(Arrays.asList(followingCombiners));

		for (NounCombiners it : combinerEnums) {
			chain.add(resolveEnum(it));
		}

	}

	/**
	 * Maps from Enum to specific instance of class.
	 *
	 * @param combinatorEnum The Enum which dependent class you want to
	 *            instantiate.
	 * @return Instantiated class for this enum.
	 */
	private ANounCombiner resolveEnum(final NounCombiners combinatorEnum) {
		switch (combinatorEnum) {
		case HawkRules:
			return SentenceToSequenceStanford.getInstance();
		case StanfordDependecy:
			return StanfordCombinedNN.getInstance();
		default:
			return null;

		}

	}

	/**
	 * Runs noun combination chain on given HAWKQuestion. This will effectively
	 * set properties in {@link HAWKQuestion#getLanguageToNounPhrases()} and
	 * will alter MutableTree of question.
	 *
	 * If you want to have a new processed Instance of HAWKQuestion, not
	 * altering the given one, check {@link #runChainReturnNew(HAWKQuestion)}
	 *
	 * @param q
	 */
	public void runChain(final HAWKQuestion q) {
		for (ANounCombiner it : chain) {
			it.combineNouns(q);
		}
	}

	/**
	 * Finds compound nouns and creates a new HAWKQuestion with processed tree,
	 * without affecting given HAWKQuestion in any way.
	 *
	 * <pre>
	 * <strong>Note:</strong> This expects expects HAWKquestion already to be dependency
	 *  parsed, e.g. a MutableTree with annotated Nodes is set.
	 *
	 * </pre>
	 *
	 * @param q HAWKQuestion to gather info from.
	 */
	public HAWKQuestion runChainReturnNew(final HAWKQuestion q) {
		HAWKQuestion qNew = HAWKQuestionFactory.createInstance(q);
		qNew.setTree(q.getTree().hardcopy());
		runChain(qNew);
		return qNew;
	}

}
