package org.aksw.qa.commons.sparql;

import java.util.List;
import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.qald.Fail;
import org.aksw.qa.commons.qald.Qald7CreationTool;
import org.aksw.qa.commons.qald.Qald7Question;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.QueryFactory;
import org.junit.Test;

public class DatasetWikidataTest {
	/**
	 * creates a health report for the wikidata qald7 datasets. be sure to set output path to your needs.
	 */
	public static void main(final String[] args) {
		String outputPath = "wikidataTest.txt";

		Qald7CreationTool tool = new Qald7CreationTool(SPARQLEndpoints.WIKIDATA_METAPHACTS, 30);
		boolean autocorrectOnlydbo = false;
		Set<Fail> ignoreFlags = Sets.newHashSet(Fail.ISONLYDBO_WRONG, Fail.MISSING_LANGUAGES);
		Set<Qald7Question> allQuestions = Sets.newHashSet();
		try {
			allQuestions = tool.loadAndAnnotateTrain(Sets.newHashSet(Dataset.QALD7_Train_Wikidata_en, Dataset.QALD7_Test_Wikidata_en), autocorrectOnlydbo);
		} catch (Exception e) {
			System.out.println("Be sure to copy the SSL certificate from metaphacts to your local JRE SSL store.\n See more @ SPARQL.ENDPOINT_WIKIDATA_METAPHACTS");
			e.printStackTrace();
		}

		for (Qald7Question question : allQuestions) {
			for (String answer : question.getServerAnswers()) {
				answer = answer.replaceAll("^^\\p{Graph}+\\s", "");
			}
		}

		tool.createFileReport(allQuestions, outputPath, ignoreFlags);
		tool.destroy();
		System.out.println("done");

	}

	@Test
	public void quickParseabilityTest() {
		List<IQuestion> questions = LoaderController.load(Dataset.QALD7_Train_Wikidata_en);
		questions.addAll(LoaderController.load(Dataset.QALD7_Test_Wikidata_en));
		for (IQuestion it : questions) {
			String sparqlQuery = it.getSparqlQuery();
			System.out.println(sparqlQuery);
			QueryFactory.create(sparqlQuery);
		}
	}

}
