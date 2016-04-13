package hawk;

import java.util.List;
import java.util.Map;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.experiment.SingleQuestionPipeline;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;

public class PosTagTest {

	StanfordNLPConnector stanford;
	List<HAWKQuestion> questionsStanford;
	List<HAWKQuestion> questionsClear;
	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);

	@Before
	public void load() {
		log.info("Starting POS-Tag comparison between StanvordNLP and ClearNLP using QALD6 Multilingual dataset");
		List<IQuestion> loadedQuestions = QALD_Loader.load(Dataset.QALD6_Train_Multilingual);
		questionsStanford = HAWKQuestionFactory.createInstances(loadedQuestions);
		questionsClear = HAWKQuestionFactory.createInstances(loadedQuestions);
		stanford = new StanfordNLPConnector();
	}

	@Test
	public void test() {

		boolean testPass = true;
		StringBuilder outputStr = new StringBuilder();
		for (HAWKQuestion currentQuestion : questionsStanford) {

			Map<String, String> core = SentenceToSequence.generatePOSTags(currentQuestion);
			Annotation doc = stanford.runAnnotation(currentQuestion);
			Map<String, String> stanPos = stanford.generatePOSTags(doc);

			if (!core.equals(stanPos)) {
				outputStr.append("CLEAR |  " + core.toString() + "\n");
				outputStr.append("STAN  |  " + stanPos.toString() + "\n\n");
				testPass = false;

			}

		}
		log.debug(outputStr.toString());
		Assert.assertTrue(testPass);
	}
}
