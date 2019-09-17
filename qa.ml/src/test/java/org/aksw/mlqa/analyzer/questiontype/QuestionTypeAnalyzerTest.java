package org.aksw.mlqa.analyzer.questiontype;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class QuestionTypeAnalyzerTest {
	private static Logger log = LoggerFactory.getLogger(QuestionTypeAnalyzerTest.class);
	
	@Test
	public void resultTypeResource() {
		QuestionTypeAnalyzer typeAnalyzer = new QuestionTypeAnalyzer();
		ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>();
		fvWekaAttributes.add(typeAnalyzer.getAttribute());
		new Instances("Test", fvWekaAttributes, 1 );
		Instance test = new DenseInstance(fvWekaAttributes.size());
		test.setValue(typeAnalyzer.getAttribute(), (String) typeAnalyzer.analyze("Who was the doctoral supervisor of Albert Einstein?"));	
		assertTrue(test.stringValue(typeAnalyzer.getAttribute()).equals("RESOURCE"));
	}
	
	@Test
	public void resultTypeBoolean() {
		log.info("Test QuestionType classification ...");
		log.debug("Initialize components ...");
		
		log.info("Run queries through components ...");	
		log.debug("Load data file: " + Dataset.QALD9_Test_Multilingual.name());
		List<IQuestion> questions = LoaderController.load(Dataset.QALD9_Test_Multilingual);
		
		int counter = 0;
		int counterASK = 0;
		int counterClassifiedWrong = 0;
		
		for (IQuestion q : questions) {			
			// Classify query type
			boolean classification = QuestionTypeAnalyzer.isASKQuestion(q.getLanguageToQuestion().get("en"));
			
			counter++;
			if (classification) {
				counterASK++;
			}
			if ( (classification && !"boolean".equals(q.getAnswerType())) || (!classification && "boolean".equals(q.getAnswerType())) ) {
				counterClassifiedWrong++;
			}
		}
			
		log.info("Classified " + counterClassifiedWrong + " wrong from " + counter + " queries. (" + counterASK + " are ASK)"); 
		assertTrue(counterClassifiedWrong == 0);
	}
}
