package org.aksw.qa.commons.load;

import java.util.List;

import org.aksw.qa.commons.datastructure.Question;
import org.junit.Assert;
import org.junit.Test;

public class LoadTest {

	@Test
	// TODO use small snippets of test files under src/test/resources instead of
	// qa-datasets
	public void loadQALD5Test() {
		List<Question> load = QALD_Loader.load(Dataset.QALD5_Test);
		Assert.assertTrue(load.size() == 59);
		for (Question q : load) {
			Assert.assertTrue(q.id > 0);
			Assert.assertNotNull(q.answerType);
			Assert.assertTrue(q.pseudoSparqlQuery != null || q.sparqlQuery != null);
			Assert.assertNotNull(q.languageToQuestion);
			Assert.assertNotNull(q.languageToKeywords);
			System.out.println(q);
			Assert.assertTrue(q.goldenAnswers != null && q.answerType.matches("resource||boolean||number||date||string"));
		}
	}
}
