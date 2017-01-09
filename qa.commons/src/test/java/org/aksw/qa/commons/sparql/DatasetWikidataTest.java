package org.aksw.qa.commons.sparql;

import java.util.Set;

import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.qald.Qald7CreationTool;
import org.aksw.qa.commons.qald.Qald7Question;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.junit.Ignore;

@Ignore
public class DatasetWikidataTest {

	public static void main(final String[] args) {
		Qald7CreationTool tool = new Qald7CreationTool(SPARQL.ENDPOINT_WIKIDATA_ORG, 30);
		boolean autocorrectOnlydbo = false;
		boolean skipQuestionsWithTooLittleLanguages = false;
		Set<Qald7Question> allQuestions = tool.loadAndAnnotateTrain(Sets.newHashSet(Dataset.QALD7_Train_Multilingual_Wikidata), false);

		for (Qald7Question question : allQuestions) {
			for (String answer : question.getServerAnswers()) {
				answer = answer.replaceAll("^^\\p{Graph}+\\s", "");
			}
		}

		tool.createFileReportForTestQuestions(Sets.newHashSet(Dataset.QALD7_Train_Multilingual_Wikidata), autocorrectOnlydbo, "c:/output/wikidataTest.txt", skipQuestionsWithTooLittleLanguages);
		tool.destroy();
		System.out.println("done");

	}

}
