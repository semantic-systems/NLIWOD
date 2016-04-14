package hawk;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
		Map<String, String> mismatched = new HashMap<String, String>();
		Map<String, Integer> mismatchCnt = new HashMap<String, Integer>();
		Map<String, Integer> totalCnt = new HashMap<String, Integer>();
		for (HAWKQuestion currentQuestion : questionsStanford) {

			Map<String, String> core = SentenceToSequence.generatePOSTags(currentQuestion);
			Annotation doc = stanford.runAnnotation(currentQuestion);
			Map<String, String> stanPos = stanford.generatePOSTags(doc);
			for (Map.Entry<String, String>e:stanPos.entrySet())
			{	
				totalCnt.putIfAbsent(e.getValue(), 0);
			//Generate new map entry for every occurance of [StanfordPos] - [CorePos]
				totalCnt.put(e.getValue(), totalCnt.get(e.getValue())+1);
			}
			if (!core.equals(stanPos)) {
				outputStr.append("CLEAR |  " + core.toString() + "\n");
				outputStr.append("STAN  |  " + stanPos.toString() + "\n");
				for (Map.Entry<String, String>e:stanPos.entrySet())
				{
					String stanKey = e.getKey();
					String stanVal=e.getValue();
					String coreVal=core.get(stanKey);


					if (!stanVal.equals(coreVal))
					{
						mismatched.putIfAbsent(stanVal, coreVal);
						String mismatchLabel=stanVal+"-"+coreVal;
						mismatchCnt.putIfAbsent(mismatchLabel, 0);
						mismatchCnt.put(mismatchLabel, mismatchCnt.get(mismatchLabel)+1);
						outputStr.append("Differing POS Tags for node '"+ stanKey + "' (Stanf.|Core):"+stanVal+ " | " + coreVal+"\n\n"+ "");
					}
				}
				testPass = false;

			}

		}
		log.debug(outputStr.toString());
		log.info("Discrepancies between POS-Tags (Stan = Clear):");
		log.info(mismatched.toString());
		log.info("Discrepancy count (Stan-Clear):");
		log.info(mismatchCnt.toString());
		log.info("Discrepancies/Total Occurrances in Stanford:");
		for (String mismatchedPOS:mismatchCnt.keySet()) 
		{
			String stanKey=mismatchedPOS.split("-")[0];

			Integer totalOcc = totalCnt.get(stanKey);
					log.info("["+mismatchedPOS + "]/["+ stanKey+"]: "+mismatchCnt.get(mismatchedPOS).toString()+ "/"+  totalOcc);
		}
		Assert.assertTrue(testPass);

	}
}
