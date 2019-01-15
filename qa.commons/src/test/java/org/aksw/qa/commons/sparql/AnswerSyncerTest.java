package org.aksw.qa.commons.sparql;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class AnswerSyncerTest {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void prefixResolverTest() {
		String query = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?uri ?string WHERE { ?uri rdf:type dbo:FormulaOneRacer . ?uri dbo:races ?x . OPTIONAL { ?uri rdfs:label ?string. FILTER (lang(?string) = 'en') } } ORDER BY DESC(?x) OFFSET 0 LIMIT 1";
		String answer = "http://dbpedia.org/resource/Michael_Schumacher";
		IQuestion question = new Question();
		question.setSparqlQuery(query);
				
		try {
			AnswerSyncer.syncAnswers(question, SPARQLEndpoints.DBPEDIA_ORG);
		} catch (ExecutionException e) {
			e.printStackTrace();
			Assert.fail();
		}		
		ArrayList<String> answers = Lists.newArrayList(question.getGoldenAnswers());

		Assert.assertTrue("More than one answer or no answer", answers.size() == 1);	
		Assert.assertTrue("Answer is not correct", answer.equals(answers.get(0)));
		log.debug("Answer: \n" + answers.get(0));
	}
}
