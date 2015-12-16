package org.aksw.mlqa.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.aksw.mlqa.datastructure.Run;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;

public class SimpleQuestionPrinter {
	public static void main(String[] args) throws FileNotFoundException {
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();

		List<Question> questions = QALD_Loader.load(Dataset.QALD5_Test);

		File QALD5Logs = new File(classLoader.getResource("QALD-5_logs/").getFile());
		List<Run> runs = SearchBestQALDResult.searchBestRun(questions, QALD5Logs);
		System.out.print("question.id" + "\t" + "questionString" + "\t");
		for (Run run : runs) {
			System.out.print(run.getName() + "\t");
		}
		System.out.println();
		for (Question question : questions) {
			if (!question.outOfScope && !question.hybrid && !question.goldenAnswers.isEmpty()) {
				String questionString = question.languageToQuestion.get("en");
				System.out.print(question.id + "\t" + questionString + "\t");
			
				for (Run run : runs) {
					Double fmeasure = 0.0;
					if (run.getMap().containsKey(questionString)) {
						fmeasure = run.getMap().get(questionString);
					}
					System.out.print(fmeasure+"\t");
				}
				
				System.out.println();
			}
		}
	}
}
