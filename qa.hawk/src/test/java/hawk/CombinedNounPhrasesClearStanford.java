package hawk;

import java.util.List;
import java.util.Map;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.experiment.SingleQuestionPipeline;
import org.aksw.hawk.nouncombination.NounCombinationChain;
import org.aksw.hawk.nouncombination.NounCombiners;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CombinedNounPhrasesClearStanford {

	StanfordNLPConnector stanford;
	List<HAWKQuestion> questionsStanford;
	List<HAWKQuestion> questionsClear;
	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);

	@Before
	public void load() {
		log.info("Starting Noun Phrase Comparison between StanvordNLP and ClearNLP using QALD6 Multilingual dataset");
		List<IQuestion> loadedQuestions = LoaderController.load(Dataset.QALD6_Train_Multilingual);
		questionsStanford = HAWKQuestionFactory.createInstances(loadedQuestions);
		questionsClear = HAWKQuestionFactory.createInstances(loadedQuestions);
		stanford = new StanfordNLPConnector();
	}

	@Test
	@Ignore
	// FIXME test does not pass. why?
	public void test() {
		boolean testPass = true;

		for (HAWKQuestion currentQuestion : questionsStanford) {

			stanford.parseTree(currentQuestion, null);
			NounCombinationChain nnChain = new NounCombinationChain(NounCombiners.StanfordDependecy);
			nnChain.runChain(currentQuestion);

		}
		for (HAWKQuestion currentQuestion : questionsClear) {
			stanford.parseTree(currentQuestion, null);
			NounCombinationChain nnChain2 = new NounCombinationChain(NounCombiners.HawkRules);
			nnChain2.runChain(currentQuestion);
		}
		for (int i = 0; i < questionsStanford.size(); i++) {
			Map<String, List<Entity>> stanfordMap = questionsStanford.get(i).getLanguageToNounPhrases();
			Map<String, List<Entity>> clearMap = questionsClear.get(i).getLanguageToNounPhrases();
			/**
			 * Check if every recognized noun phrase from ClearNLP is also
			 * recognized by StanfordNLP
			 *
			 */

			List<Entity> stanfordList = stanfordMap.get("en");
			List<Entity> clearList = clearMap.get("en");

			if (!((stanfordList == null) || (clearList == null))) {

				if (!stanfordList.containsAll(clearList)) {
					log.debug("DOESNT CONTAIN ALL KEYS");
					log.debug("StanfordList " + stanfordList.toString());
					log.debug("ClearList " + clearList.toString());
					testPass = false;
				}
			}
			/**
			 * Not pretty but you can atleast see all wrong parts in log
			 */
			Assert.assertTrue(testPass);

		}

	}
}
