package org.aksw.qa.commons.load;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//FIXME In general, replace all System.out calls with proper log.debug calls 
public class LoadTest {
	Logger log = LoggerFactory.getLogger(LoadTest.class);

	@Test
	/**
	 * Testing all datasets for loadability and validity of SPARQL-queries
	 */
	// FIXME do not ignore that but also
	// @Ignore
	public void testAllDatasetsTowardsLoadibility() {
		Boolean queriesValid = true;
		for (Dataset d : Dataset.values()) {
			log.info("Try to load:" + d.name());
			try {
				List<IQuestion> questions = LoaderController.load(d);
				log.info("Dataset succesfully loaded:" + d.name());

				for (IQuestion q : questions) {
					// TODO enable that once the answer type of NLQ is fixed
					// Assert.assertNotNull(q.toString(), q.getAnswerType());
					Assert.assertTrue(q.getPseudoSparqlQuery() != null || q.getSparqlQuery() != null || d.equals(Dataset.Stanford_dev));
					if (q.getSparqlQuery() != null) {
						queriesValid = (execQueryWithoutResults(q) && queriesValid);
					}
					Assert.assertNotNull(q.getLanguageToQuestion());
					Assert.assertFalse(q.getLanguageToQuestion().values().isEmpty());
					Assert.assertNotNull(q.getLanguageToKeywords());
					Assert.assertTrue(q.toString(), q.getGoldenAnswers() != null);
					// FIXME sobald wir auf das eigentliche QALD repository
					// commiten können nimm hier und in QALD 5 den antworttyp
					// "uri" "list" raus
					// "list" und "uri" sollten vom loader auf URI gemappt
					// werden
					// "num" sollte auf number gemappt werden
					// Assert.assertTrue(q.toString(),
					// q.getAnswerType().matches("resource||uri||list||boolean||number||date||string"));
				}

			} catch (Exception e) {
				log.error("Dataset couldn't be loaded:" + d.name());
			}
		}
		// TODO fix this

		// Assert.assertTrue(queriesValid);
	}

	@Test
	public void loadStanfordTest() {
		List<IQuestion> load = LoaderController.load(Dataset.Stanford_dev);
		log.debug("Size of Dataset: " + load.size());
		Assert.assertTrue(load.size() == 2067);
		for (IQuestion q : load) {
			Assert.assertNotNull(q.getLanguageToQuestion());
			Assert.assertFalse(q.getLanguageToQuestion().values().isEmpty());
			Assert.assertTrue(q.getGoldenAnswers() != null&&q.getGoldenAnswers().size()>0);
		}
	}

	@Test
	public void loadQALD5Test() {
		List<IQuestion> load = LoaderController.load(Dataset.QALD5_Test_Hybrid);
		log.debug("Size of Dataset: " + load.size());
		Assert.assertTrue(load.size() == 10);
		for (IQuestion q : load) {
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
		List<IQuestion> load = LoaderController.load(Dataset.QALD6_Train_Multilingual);
		List<Integer> incompletes = Arrays.asList(100, 118, 136, 137, 147, 152, 94, 95, 96, 97, 98, 99, 249, 250, 312, 340, 342);
		log.debug("Number of Loaded Questions:" + load.size());
		Assert.assertTrue(load.size() == 350 - incompletes.size());
		for (IQuestion q : load) {
			Assert.assertNotNull(q.getAnswerType());
			Assert.assertTrue(q.getGoldenAnswers() != null && q.getAnswerType().matches("resource||boolean||number||date||string"));
			Assert.assertNotNull(q.getLanguageToQuestion());
			Assert.assertFalse(q.getLanguageToQuestion().values().isEmpty());
			Assert.assertNotNull(q.getLanguageToKeywords());
			// skipping Answer on known incompletes:
			if (!incompletes.contains(q.getId())) {
				Assert.assertTrue(q.getPseudoSparqlQuery() != null || q.getSparqlQuery() != null);
				// log.debug(q.getLanguageToQuestion().get("en") + "\t" +
				// "Answer:" + "\t" + q.getSparqlQuery());
			} else {
				// log.debug(q.getLanguageToQuestion().get("en") + "\t" +
				// "No Answer, known incomplete question.");
			}
		}

	}

	boolean execQuery(final IQuestion q, final boolean hurry) {

		Query query = new Query();

		// Execute the query and obtain results
		QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp("http://139.18.2.164:3030/ds/sparql");
		Boolean queryValid = true;
		if (hurry) {
			log.debug("Testing query for parsability: " + q.getId() + ": " + q.getLanguageToQuestion().get("en"));
		} else {
			log.debug("Testing query for parsability and returned results: " + q.getId() + ": " + q.getLanguageToQuestion().get("en"));
		}
		try {
			query = QueryFactory.create(q.getSparqlQuery());

			// Execute the query and obtain results

			QueryExecution qe = qef.createQueryExecution(query.toString());

			if (!q.getGoldenAnswers().isEmpty()) {
				if (!hurry) {
					ResultSet results = qe.execSelect();

					if (results.toString().contains(q.getGoldenAnswers().toString())) {
						log.info("Question result doesn't contain golden answer!");
						log.info("Actual results: " + results.toString());
						log.info("Golden answers: " + q.getGoldenAnswers().toString());
					}
				}

				log.debug("Query valid for q" + q.getId() + " - " + q.getLanguageToQuestion().get("en"));
				// log.debug(query.toString());
				queryValid = (queryValid && true);
				qe.close();
			}
		} catch (Exception e) {
			// FIXME bereits hier eine Assertion mit Message einbauen sonst
			// sieht man oben nur das das flag false ist aber nicht warum und
			// wieso
			if (e.getClass() != org.apache.jena.sparql.resultset.ResultSetException.class) {

				log.debug(q.getSparqlQuery());
				log.info("Jena error: " + e.toString());
				log.info("!!!! Query invalid for q" + q.getId() + " - " + q.getLanguageToQuestion().get("en"));
				queryValid = false;
			} else {
				if (q.getGoldenAnswers().isEmpty()) {
					log.debug("Query delivers no results for q" + q.getId() + " (expecting: empty) - " + q.getLanguageToQuestion().get("en"));
					queryValid = (queryValid && true);
				} else {
					if (q.getGoldenAnswers().contains("true") || q.getGoldenAnswers().contains("false")) {
						log.debug("Query delivers no results for q" + q.getId() + " (expecting: boolean) - " + q.getLanguageToQuestion().get("en"));
						queryValid = (queryValid && true);
					}
					log.info("Golden answer not returned! Expecting:" + q.getGoldenAnswers().toString());
					queryValid = false;
				}

			}
		}
		return queryValid;
	}

	boolean execQuery(final IQuestion q) {
		return execQuery(q, false);
	}

	boolean execQueryWithoutResults(final IQuestion q) {
		return execQuery(q, true);
	}
}