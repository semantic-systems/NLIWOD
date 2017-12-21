package org.aksw.hawk.querybuilding;



import java.util.List;
import java.util.concurrent.ExecutionException;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.number.UnitController;
import org.json.simple.parser.ParseException;
import org.junit.Test;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternSparqlGeneratorTest {
	private static Logger log = LoggerFactory.getLogger(PatternSparqlGeneratorTest.class);

	@Test
	public void testForZeroPropertiesClassesEntities() throws ExecutionException, RuntimeException, ParseException {
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "hey");
		StanfordNLPConnector stanfordConnector = new StanfordNLPConnector();
		UnitController numberToDigit = new UnitController();
		numberToDigit.instantiateEnglish(stanfordConnector);
		q.setTree(stanfordConnector.parseTree(q, numberToDigit));
		
		PatternSparqlGenerator patternsparqlgenerator = new PatternSparqlGenerator();
		List<Answer> answers = patternsparqlgenerator.build(q);
		Assert.assertTrue(answers.isEmpty());
	}

}
