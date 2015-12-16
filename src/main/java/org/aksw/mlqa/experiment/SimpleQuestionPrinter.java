package org.aksw.mlqa.experiment;

import java.util.List;

import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;

public class SimpleQuestionPrinter {
	public static void main(String[] args) {
		List<Question> questions = QALD_Loader.load(Dataset.QALD5_Test);
		for (Question question : questions) {
			if (!question.outOfScope && !question.hybrid && !question.goldenAnswers.isEmpty()) {
				System.out.println(question.id + "\t" + question.languageToQuestion.get("en"));
			}
		}
	}
}
