package org.aksw.hawk.nouncombination;

import java.util.Arrays;
import java.util.List;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NounCombinationChainTest {
	private static Logger log = LoggerFactory.getLogger(NounCombinationChainTest.class);

	/**
	 * Tests if processing tree works properly, doesnt check whether the right
	 * combinedNNs are found.
	 */
	@Test
	public void test() {
		NounCombinationChain hawk = new NounCombinationChain(NounCombiners.HawkRules);
		NounCombinationChain stanf = new NounCombinationChain(NounCombiners.StanfordDependecy);

		NounCombinationChain hawkStanf = new NounCombinationChain(NounCombiners.HawkRules, NounCombiners.StanfordDependecy);
		NounCombinationChain stanfHawk = new NounCombinationChain(NounCombiners.StanfordDependecy, NounCombiners.HawkRules);

		List<HAWKQuestion> questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD6_Test_Multilingual));

		StanfordNLPConnector parse = new StanfordNLPConnector();
		//@formatter:off
		String[] systemicWrong={
			"Who was on the Apollo 11 mission?", 						//Finds "apollo mission" as combinedNN, ignores the 11
			"Which movies star both Liz Taylor and Richard Burton?",	// Duplicate Node "Burton"
			"Who is the son of Sonny and Cher?"							//Duplicate Node "Cher"
		};
		//@formatter:on

		for (HAWKQuestion q : questions) {
			parse.parseTree(q, null);
			String qString = q.getLanguageToQuestion().get("en");
			if (Arrays.asList(systemicWrong).contains(qString)) {
				continue;
			}

			String[] reengineer = { printQuestionFromTree(hawk.runChainReturnNew(q)), printQuestionFromTree(stanf.runChainReturnNew(q)), printQuestionFromTree(hawkStanf.runChainReturnNew(q)),
			        printQuestionFromTree(stanfHawk.runChainReturnNew(q)) };

			for (String it : reengineer) {
				if (!qString.equals(it)) {
					log.debug("Couldnt reengineer question from parsed tree\n should be:\n|" + qString + "|\nbut is:\n|" + it + "|");
					{
						Assert.fail();
					}
				}

			}

		}

	}

	private static String printQuestionFromTree(final HAWKQuestion q) {
		MutableTree tree = q.getTree();
		String out = "";
		for (MutableTreeNode it : tree.getAllNodesInSentenceOrder()) {
			out += it.getLabel() + " ";
		}
		return out.replaceAll("(\\s+)(\\p{Punct})(\\s*)$", "$2").replaceAll("(\\s+)(')", "$2").trim();

	}

}
