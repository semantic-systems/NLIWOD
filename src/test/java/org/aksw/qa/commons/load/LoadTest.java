package org.aksw.qa.commons.load;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.junit.Assert;
import org.junit.Test;

public class LoadTest {

	@Test
	// TODO use small snippets of test files under src/test/resources instead of
	// qa-datasets so we can get rid of the dependency
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
			System.out.println(q);
			Assert.assertTrue(q.getGoldenAnswers() != null && q.getAnswerType().matches("resource||boolean||number||date||string"));
		}
	}
	
	@Test
	// TODO use small snippets of test files under src/test/resources instead of
	// qa-datasets so we can get rid of the dependency
	//TODO update test
	public void loadQALD6Test() throws IOException {
		//URL url= ClassLoader.getSystemClassLoader().getResource("QALD-6/qald-6-train-multilingual.json");
		List<IQuestion> load = QALD_Loader.load(Dataset.QALD6_Train_Multilingual);
		Assert.assertTrue(load.size() == 350);
		List<Integer>incompletes=Arrays.asList(100, 118, 136, 137, 147, 152, 94, 95, 96, 97, 98, 99, 249, 250, 312, 340, 342);
		for (IQuestion q : load) {
			System.out.print("Testing: "+q.getId()+"\t");
			Assert.assertTrue(q.getId() > 0);
			Assert.assertNotNull(q.getAnswerType());
			Assert.assertTrue(q.getGoldenAnswers() != null && q.getAnswerType().matches("resource||boolean||number||date||string"));
			Assert.assertNotNull(q.getLanguageToQuestion());
			Assert.assertFalse(q.getLanguageToQuestion().values().isEmpty());
			Assert.assertNotNull(q.getLanguageToKeywords());
			
			//skipping Answer on known incompletes:
			if (!incompletes.contains(q.getId()))
			{
				
			
				Assert.assertTrue(q.getPseudoSparqlQuery() != null || q.getSparqlQuery() != null);
				System.out.println(q.getLanguageToQuestion().get("en")+"\t"+"Answer:"+"\t"+q.getSparqlQuery());
				}
			else
			{
				System.out.println(q.getLanguageToQuestion().get("en")+"\t"+"No Answer, known incomplete question.");
			}
		}
	}
}