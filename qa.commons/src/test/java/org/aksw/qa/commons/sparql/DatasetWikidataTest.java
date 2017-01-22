package org.aksw.qa.commons.sparql;

import java.util.Set;

import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.qald.Fail;
import org.aksw.qa.commons.qald.Qald7CreationTool;
import org.aksw.qa.commons.qald.Qald7Question;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.junit.Ignore;

@Ignore
public class DatasetWikidataTest {

	public static void main(final String[] args) {
		Qald7CreationTool tool = new Qald7CreationTool(SPARQL.ENDPOINT_WIKIDATA_METAPHACTS, 30);
		boolean autocorrectOnlydbo = false;
		Set<Fail> ignoreFlags = Sets.newHashSet(Fail.ISONLYDBO_WRONG, Fail.MISSING_LANGUAGES);
		Set<Qald7Question> allQuestions = Sets.newHashSet();
		try {
			allQuestions = tool.loadAndAnnotateTrain(Sets.newHashSet(Dataset.QALD7_Train_Multilingual_Wikidata), autocorrectOnlydbo);
		} catch (Exception e) {
			System.out.println("Be sure to copy the SSL certificate from metaphacts to your local JRE SSL store.\n See more @ SPARQL.ENDPOINT_WIKIDATA_METAPHACTS");
			e.printStackTrace();
		}

		for (Qald7Question question : allQuestions) {
			for (String answer : question.getServerAnswers()) {
				answer = answer.replaceAll("^^\\p{Graph}+\\s", "");
			}
		}

		tool.createFileReport(allQuestions, "c:/output/wikidataTest.txt", ignoreFlags);
		tool.destroy();
		System.out.println("done");

	}

}
