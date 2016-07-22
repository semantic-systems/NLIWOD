package hawk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.experiment.SingleQuestionPipeline;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;

/**
 * Tests the POS tags attributed to questions by either Stanford NLP or Core NLP
 * tagging methods.
 * 
 * @author jhuthmann, dec
 *
 */
public class PosTagTest {

	StanfordNLPConnector stanford;
	List<HAWKQuestion> questionsStanford;
	List<HAWKQuestion> questionsClear;
	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);

	@Before
	public void load() {
		log.info("Starting POS-Tag comparison between StanvordNLP and ClearNLP using QALD6 Multilingual dataset");

		List<IQuestion> loadedQuestions = LoaderController.load(Dataset.QALD6_Train_Multilingual);
		questionsStanford = HAWKQuestionFactory.createInstances(loadedQuestions);
		questionsClear = HAWKQuestionFactory.createInstances(loadedQuestions);
		stanford = new StanfordNLPConnector();
	}

	@Test
	@Ignore
	//FIXME does not pass, why?
	public void test() {

		boolean testPass = true;
		StringBuilder outputStr = new StringBuilder();
		Map<String, String> mismatched = new HashMap<String, String>();
		Map<String, Integer> mismatchCnt = new HashMap<String, Integer>();
		Map<String, Integer> stanTotalCnt = new HashMap<String, Integer>();
		Map<String, Integer> coreTotalCnt = new HashMap<String, Integer>();
		for (HAWKQuestion currentQuestion : questionsStanford) {

			Map<String, String> core = SentenceToSequence.generatePOSTags(currentQuestion);
			Annotation doc = stanford.runAnnotation(currentQuestion);
			Map<String, String> stanPos = stanford.generatePOSTags(doc);
			for (Map.Entry<String, String> e : stanPos.entrySet()) {

				stanTotalCnt.putIfAbsent(e.getValue(), 0);

				// Generate new map entry for every occurance of [StanfordPos] -
				// [CorePos]
				stanTotalCnt.put(e.getValue(), stanTotalCnt.get(e.getValue()) + 1);
				String stanKey = e.getKey();
				String stanVal = e.getValue();
				String coreVal = core.get(stanKey);
				if (coreVal == null) {
					coreVal = "null";
				}
				if (stanVal == null) {
					stanVal = "null";
				}
				coreTotalCnt.putIfAbsent(coreVal, 0);
				coreTotalCnt.put(coreVal, coreTotalCnt.get(coreVal) + 1);

				if (!stanVal.equals(coreVal)) {
					mismatched.putIfAbsent(stanVal, coreVal);
					String mismatchLabel = stanVal + "-" + coreVal;
					mismatchCnt.putIfAbsent(mismatchLabel, 0);
					mismatchCnt.put(mismatchLabel, mismatchCnt.get(mismatchLabel) + 1);
					outputStr.append("Differing POS Tags for node '" + stanKey + "' (Stanf.|Core):" + stanVal + " | " + coreVal + "\n\n" + "");
				}
			}
			if (!core.equals(stanPos)) {
				outputStr.append("CLEAR |  " + core.toString() + "\n");
				outputStr.append("STAN  |  " + stanPos.toString() + "\n");

				testPass = false;

			}

		}
		log.debug(outputStr.toString());
		log.info("Discrepancies between POS-Tags (Stan = Clear):");
		log.info(mismatched.toString());
		log.info("Discrepancy count (Stan-Clear):");
		log.info(mismatchCnt.toString());
		log.info("Discrepancies/Total Occurrances of POS  in Stanford and CoreNLP");
		for (String mismatchedPOS : mismatchCnt.keySet()) {
			String stanKey = mismatchedPOS.split("-")[0];
			String coreKey = mismatchedPOS.split("-")[1];
			Integer totalStanOcc = stanTotalCnt.get(stanKey);
			Integer totalCoreOcc = coreTotalCnt.get(coreKey);
			Integer totalStanOcc2 = stanTotalCnt.get(coreKey);
			Integer totalCoreOcc2 = coreTotalCnt.get(stanKey);
			log.info("[" + mismatchedPOS + "]:" + mismatchCnt.get(mismatchedPOS).toString());
			log.info("[" + stanKey + "]/[" + coreKey + "] (StanfordNLP): " + totalStanOcc + "/" + totalStanOcc2);
			log.info("[" + stanKey + "]/[" + coreKey + "] (CoreNLP): " + totalCoreOcc2 + "/" + totalCoreOcc);
		}
		Assert.assertTrue(testPass);

	}
}
