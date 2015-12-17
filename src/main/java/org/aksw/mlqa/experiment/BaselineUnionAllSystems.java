package org.aksw.mlqa.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.mlqa.datastructure.Run;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.aksw.qa.commons.measure.AnswerBasedEvaluation;

public class BaselineUnionAllSystems {
	public static void main(String[] args) throws FileNotFoundException {
		List<Question> QALD5Question = QALD_Loader.load(Dataset.QALD5_Test);

		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		File QALD5Logs = new File(classLoader.getResource("QALD-5_logs/").getFile());

		List<Run> runs = SearchBestQALDResult.searchBestRun(QALD5Question, QALD5Logs);

		for (Question goldSystemQuestion : QALD5Question) {
			if (!goldSystemQuestion.outOfScope && !goldSystemQuestion.hybrid && !goldSystemQuestion.goldenAnswers.isEmpty()) {
				// get the answerset of each approach and union it, measure
				Set<String> answerset = new HashSet<String>();
				for (Run run : runs) {
					List<Question> questions = QALD_Loader.load(new FileInputStream(run.getSubmission()));
					for (Question question : questions) {
						if (goldSystemQuestion.id == question.id) {
							answerset.addAll(question.goldenAnswers);
						}
					}
				}
				double fmeasure = AnswerBasedEvaluation.fMeasure(answerset, goldSystemQuestion);
				System.out.println(fmeasure);
			}
		}

	}
}
