package org.aksw.qa.commons.load;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadTest {
	Logger log = LoggerFactory.getLogger(LoadTest.class);

	@Test
	public void testAllDatasetsTowardsLoadibility() {
		for (Dataset d : Dataset.values()) {
			try {
				List<IQuestion> questions = QALD_Loader.load(d);
				log.info("Dataset succesfully loaded:" + d.name());
				for (IQuestion q : questions) {
					Assert.assertTrue(q.getId() > 0);
					Assert.assertNotNull(q.getAnswerType());
					Assert.assertTrue(q.getPseudoSparqlQuery() != null || q.getSparqlQuery() != null);
					Assert.assertNotNull(q.getLanguageToQuestion());
					Assert.assertFalse(q.getLanguageToQuestion().values().isEmpty());
					Assert.assertNotNull(q.getLanguageToKeywords());
					Assert.assertTrue(q.getGoldenAnswers() != null && q.getAnswerType().matches("resource||boolean||number||date||string"));
				}
			} catch (Exception e) {
				log.error("Dataset couldn't be loaded:" + d.name());
			}
		}
	}

	@Test
	public void loadQALD5Test() {
		List<IQuestion> load = QALD_Loader.load(Dataset.QALD5_Test);
		Assert.assertTrue(load.size() == 59);
		for (IQuestion q : load) {
			Assert.assertTrue(q.getId() > 0);
			Assert.assertNotNull(q.getAnswerType());
			Assert.assertTrue(q.getPseudoSparqlQuery() != null || q.getSparqlQuery() != null);
			Assert.assertNotNull(q.getLanguageToQuestion());
			Assert.assertFalse(q.getLanguageToQuestion().values().isEmpty());
			Assert.assertNotNull(q.getLanguageToKeywords());
			Assert.assertTrue(q.getGoldenAnswers() != null && q.getAnswerType().matches("resource||boolean||number||date||string"));
		}
	}

	@Test
	public void loadQALD6Test_Multilingual() throws IOException {
		List<IQuestion> load = QALD_Loader.load(Dataset.QALD6_Train_Multilingual);
		List<Integer> incompletes = Arrays.asList(100, 118, 136, 137, 147, 152, 94, 95, 96, 97, 98, 99, 249, 250, 312, 340, 342);
		log.debug("Number of Loaded Questions:" + load.size());
		Assert.assertTrue(load.size() == 350 - incompletes.size());
		for (IQuestion q : load) {
			Assert.assertTrue(q.getId() > 0);
			Assert.assertNotNull(q.getAnswerType());
			Assert.assertTrue(q.getGoldenAnswers() != null && q.getAnswerType().matches("resource||boolean||number||date||string"));
			Assert.assertNotNull(q.getLanguageToQuestion());
			Assert.assertFalse(q.getLanguageToQuestion().values().isEmpty());
			Assert.assertNotNull(q.getLanguageToKeywords());

			// skipping Answer on known incompletes:
			if (!incompletes.contains(q.getId())) {
				Assert.assertTrue(q.getPseudoSparqlQuery() != null || q.getSparqlQuery() != null);
				log.debug(q.getLanguageToQuestion().get("en") + "\t" + "Answer:" + "\t" + q.getSparqlQuery());
			} else {
				log.debug(q.getLanguageToQuestion().get("en") + "\t" + "No Answer, known incomplete question.");
			}
		}
	}

}