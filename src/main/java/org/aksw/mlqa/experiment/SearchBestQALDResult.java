package org.aksw.mlqa.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.mlqa.datastructure.Run;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.aksw.qa.commons.measure.AnswerBasedEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchBestQALDResult {
	static Logger log = LoggerFactory.getLogger(SearchBestQALDResult.class);

	public static void main(String[] args) throws IOException, URISyntaxException {
		List<Question> QALD5Question = QALD_Loader.load(Dataset.QALD5_Train);

		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		File QALD5Logs = new File(classLoader.getResource("QALD-5_logs/").getFile());

		searchBestRun(QALD5Question, QALD5Logs);
	}

	private static List<Run> searchBestRun(List<Question> QALD5Question, File QALD5Logs) throws FileNotFoundException {
		List<Run> runs = new ArrayList<Run>();
		for (File system : QALD5Logs.listFiles()) {
			double fMax = 0;
			String submissionMax = null;
			for (File submission : system.listFiles()) {
				List<Question> questions = QALD_Loader.load(new FileInputStream(submission));
				double averageFMeasure = 0;
				int count = 0;
				// find matching questions
				for (Question goldSystemQuestion : QALD5Question) {
					for (Question question : questions) {
						if (goldSystemQuestion.id == question.id && !goldSystemQuestion.outOfScope && !question.hybrid && !question.goldenAnswers.isEmpty()) {
							double fmeasure = AnswerBasedEvaluation.fMeasure(question.goldenAnswers, goldSystemQuestion);
							averageFMeasure += fmeasure;
							count++;
						}
					}
				}
				averageFMeasure = averageFMeasure / count;
				if (fMax < averageFMeasure) {
					fMax = averageFMeasure;
					submissionMax = submission.getName();
				}
			}
			log.info(system.getName() + "\t" + submissionMax + "\t" + fMax);
			Run run = new Run(system.getName(), submissionMax, fMax);
			runs.add(run);
		}
		return runs;
	}

}
