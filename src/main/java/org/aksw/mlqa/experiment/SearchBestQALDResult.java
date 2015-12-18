package org.aksw.mlqa.experiment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.mlqa.datastructure.Run;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.aksw.qa.commons.measure.AnswerBasedEvaluation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO remove as much as possible static accesses
public class SearchBestQALDResult {
    static Logger log = LoggerFactory.getLogger(SearchBestQALDResult.class);

    public static void main(String[] args) throws IOException, URISyntaxException {
        List<Question> QALD5Question = QALD_Loader.load(Dataset.QALD5_Train);

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File QALD5Logs = new File(classLoader.getResource("QALD-5_logs/").getFile());

        searchBestRun(QALD5Question, QALD5Logs);

        QALD5Question = QALD_Loader.load(Dataset.QALD5_Test);

        QALD5Logs = new File(classLoader.getResource("QALD-5_logs/").getFile());

        searchBestRun(QALD5Question, QALD5Logs);
    }


    public static List<Question> filterQuestions(List<Question> questions) {
        List<Question> filteredQuestions = new ArrayList<Question>(questions.size());
        for (Question question : questions) {
            if (!question.outOfScope && !question.hybrid && !question.goldenAnswers.isEmpty()) {
                filteredQuestions.add(question);
            }
        }
        return filteredQuestions;
    }

    static List<Run> searchBestRun(List<Question> goldStandardQuestions, File FolderWithSystemLogs)
            throws FileNotFoundException {
        List<Run> runs = new ArrayList<Run>();
        for (File system : FolderWithSystemLogs.listFiles()) {
            Run run = new Run(system.getName());
            double fMax = 0;
            String submissionMax = null;
            Map tmpResultMapMax = null;
            for (File submission : system.listFiles()) {
                List<Question> questions = QALD_Loader.load(new FileInputStream(submission));
                double averageFMeasure = 0;
                // find matching questions
                Map tmpResultMap = new HashMap<String, Double>();
                for (Question goldSystemQuestion : goldStandardQuestions) {
                    if (!goldSystemQuestion.outOfScope && !goldSystemQuestion.hybrid
                            && !goldSystemQuestion.goldenAnswers.isEmpty()) {
                        for (Question question : questions) {
                            if (goldSystemQuestion.id == question.id) {
                                double fmeasure = AnswerBasedEvaluation.fMeasure(question.goldenAnswers,
                                        goldSystemQuestion);
                                averageFMeasure += fmeasure;
                                tmpResultMap.put(goldSystemQuestion.languageToQuestion.get("en"), fmeasure);
                            }
                        }
                    }
                }
                averageFMeasure = averageFMeasure / goldStandardQuestions.size();
                if (fMax < averageFMeasure) {
                    fMax = averageFMeasure;
                    submissionMax = submission.getAbsolutePath();
                    tmpResultMapMax = tmpResultMap;
                }
            }
            log.info(system.getName() + "\t" + submissionMax + "\t" + fMax);
            run.setFmeasure(fMax);
            run.setSubmission(submissionMax);
            run.setMap(tmpResultMapMax);
            runs.add(run);
        }
        return runs;
    }
}
