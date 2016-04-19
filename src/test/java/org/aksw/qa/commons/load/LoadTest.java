package org.aksw.qa.commons.load;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class LoadTest {
	Logger log = LoggerFactory.getLogger(LoadTest.class);

	@Test
	/**
	 * Testing all datasets for loadability and validity of SPARQL-queries 
	 */
	public void testAllDatasetsTowardsLoadibility() {
		Boolean queriesValid=true;
		for (Dataset d : Dataset.values()) {

			try {
				List<IQuestion> questions = QALD_Loader.load(d);
				log.info("Dataset succesfully loaded:" + d.name());


				for (IQuestion q : questions) {
					Assert.assertTrue(q.getId() > 0);
					Assert.assertNotNull(q.toString(),q.getAnswerType());
					Assert.assertTrue(q.getPseudoSparqlQuery() != null || q.getSparqlQuery() != null);
					if (q.getSparqlQuery()!=null){
						queriesValid=(execQueryWithoutResults(q) && queriesValid);	
					}
					Assert.assertNotNull(q.getLanguageToQuestion());
					Assert.assertFalse(q.getLanguageToQuestion().values().isEmpty());
					Assert.assertNotNull(q.getLanguageToKeywords());
					Assert.assertTrue(q.toString(), q.getGoldenAnswers() != null);
					// FIXME sobald wir auf das eigentliche QALD repository
					// commiten können nimm hier und in QALD 5 den antworttyp
					// "list" und "uri" raus
					Assert.assertTrue(q.toString(), q.getAnswerType().matches("resource||uri||list||boolean||number||date||string"));
				}

			} catch (Exception e) {
				log.error("Dataset couldn't be loaded:" + d.name());
			}
		}
		Assert.assertTrue(queriesValid);
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
				// log.debug(q.getLanguageToQuestion().get("en") + "\t" +
				// "Answer:" + "\t" + q.getSparqlQuery());
			} else {
				// log.debug(q.getLanguageToQuestion().get("en") + "\t" +
				// "No Answer, known incomplete question.");
			}
		}


	}

	boolean execQuery(IQuestion q, boolean hurry)
	{

		Query query = new Query();

		// Execute the query and obtain results
		QueryExecutionFactoryHttp qef = new QueryExecutionFactoryHttp("http://139.18.2.164:3030/ds/sparql");
		Boolean queryValid=true;
		if (hurry)
		{
			log.debug("Testing query for parsability: "+q.getId()+": "+q.getLanguageToQuestion().get("en"));
		}
		else
		{
			log.debug("Testing query for parsability and returned results: "+q.getId()+": "+q.getLanguageToQuestion().get("en"));
		}
		try{
			query = QueryFactory.create(q.getSparqlQuery());

			// Execute the query and obtain results

			QueryExecution qe = qef.createQueryExecution(query);
			if (!q.getGoldenAnswers().isEmpty()){
				if (!hurry)
				{
					ResultSet results = qe.execSelect();

					if  (results.toString().contains(q.getGoldenAnswers().toString())){
						log.info("Question result doesn't contain golden answer!");
						log.info("Actual results: "+ results.toString());
						log.info("Golden answers: "+ q.getGoldenAnswers().toString());
					}
				}

				log.debug("Query valid for q"+ q.getId()+ " - "+ q.getLanguageToQuestion().get("en"));
				//		log.debug(query.toString());
				queryValid=(queryValid && true);
				qe.close();
			}
		}
		catch(Exception e){
			if (e.getClass()!=com.hp.hpl.jena.sparql.resultset.ResultSetException.class){


				log.debug(q.getSparqlQuery());
				log.info("Jena error: "+e.toString());
				log.info("!!!! Query invalid for q"+ q.getId()+ " - "+ q.getLanguageToQuestion().get("en"));
				queryValid=false;
			}
			else {
				if (q.getGoldenAnswers().isEmpty()){
					log.debug("Query delivers no results for q"+ q.getId()+ " (expecting: empty) - "+ q.getLanguageToQuestion().get("en"));
					queryValid=(queryValid && true);
				}
				else {
					if (q.getGoldenAnswers().contains("true") || q.getGoldenAnswers().contains("false"))
					{
						log.debug("Query delivers no results for q"+ q.getId()+ " (expecting: boolean) - "+ q.getLanguageToQuestion().get("en"));
						queryValid=(queryValid && true);
					}
					log.info("Golden answer not returned! Expecting:" + q.getGoldenAnswers().toString());
					queryValid=false;
				}

			}
		}
		return queryValid;
	}
	boolean execQuery(IQuestion q)
	{
		return execQuery(q, false);
	}
	boolean execQueryWithoutResults(IQuestion q)
	{
		return execQuery(q, true);
	}
}