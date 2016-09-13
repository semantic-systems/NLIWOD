package org.aksw.hawk.controller;

import java.util.List;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.junit.Assert;
import org.junit.Test;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;

public class StanfordNLPConnectorTest {
	StanfordNLPConnector connector = new StanfordNLPConnector();
	List<HAWKQuestion> questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD6_Test_Hybrid));

	@Test
	public void prePostProcessTest() {
		for (HAWKQuestion q : questions) {
			String qString = q.getLanguageToQuestion().get("en");
			String pre = connector.preprocessStringForStanford(qString);
			CoreLabel coreLabel = new CoreLabel();
			coreLabel.setWord(pre);
			IndexedWord word = new IndexedWord(coreLabel);
			String post = connector.postprocessStringForStanford(word);
			Assert.assertTrue("|" + post + "| should be |" + qString + "|", post.equals(qString));
		}

	}

	/**
	 * Yes, really. What else to test here?? Logic of parsing is not our
	 * business, transforming StanfordGraph to MutableTree is very hard to test
	 * another way.
	 */
	@Test
	public void parseTest() {
		HAWKQuestion q = new HAWKQuestion("Who was vice president under the president who approved the use of atomic weapons against Japan during World War II?");
		MutableTree tree = connector.parseTree(q, null);
		//@formatter:off
		String shouldBe=
				"\n>Who (0|WP|0)\n"+
				"|=>was (1|VBD|0)\n"+
				"|=>president (2|NN|0)\n"+
				"|==>vice (3|NN|0)\n"+
				"|==>president (4|NN|0)\n"+
				"|===>under (5|IN|0)\n"+
				"|===>the (6|DT|0)\n"+
				"|===>approved (7|VBD|0)\n"+
				"|====>Japan (8|NNP|0)\n"+
				"|=====>against (9|IN|0)\n"+
				"|====>II (10|NNP|0)\n"+
				"|=====>during (11|IN|0)\n"+
				"|=====>World (12|NNP|0)\n"+
				"|=====>War (13|NNP|0)\n"+
				"|====>who (14|WP|0)\n"+
				"|====>use (15|NN|0)\n"+
				"|=====>the (16|DT|0)\n"+
				"|=====>weapons (17|NNS|0)\n"+
				"|======>of (18|IN|0)\n"+
				"|======>atomic (19|JJ|0)\n"+
				"|=>? (20|.|0)\n";
		//@formatter:on

		Assert.assertTrue("Wrong tree\n" + tree.toString(), tree.toString().equals(shouldBe));
	}

}
