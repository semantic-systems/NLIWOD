package org.aksw.hawk.nouncombination;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.junit.Assert;
import org.junit.Test;

public class StanfordCombinedNNTest extends ANounCombinatorTest {
	StanfordNLPConnector connector = new StanfordNLPConnector();
	NounCombinationChain chain = new NounCombinationChain(NounCombiners.StanfordDependecy);

	@Test
	public void test1() {

		HAWKQuestion q = new HAWKQuestion("Who was vice president under the president who authorized atomic weapons against Japan during World War II?");
		connector.parseTree(q, null);

		chain.runChain(q);
		Assert.assertTrue(containsExactly(q.getLanguageToNounPhrases().get("en"), "vice president", "World War II"));

	}

	@Test
	public void test2() {
		HAWKQuestion q = new HAWKQuestion("Give me all Australian metalcore bands.");
		connector.parseTree(q, null);

		chain.runChain(q);
		System.out.println(q.getLanguageToNounPhrases().get("en").toString());
		Assert.assertTrue(containsExactly(q.getLanguageToNounPhrases().get("en"), "metalcore bands"));
	}

	@Test
	public void test3() {
		HAWKQuestion q = new HAWKQuestion("Which ingredients do I need for carrot cake?");
		connector.parseTree(q, null);

		chain.runChain(q);
		System.out.println(q.getLanguageToNounPhrases().get("en").toString());
		Assert.assertTrue(containsExactly(q.getLanguageToNounPhrases().get("en"), "carrot cake"));
	}

}
