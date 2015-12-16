package org.aksw.mlqa.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.mlqa.analyzer.Analyzer;
import org.aksw.mlqa.datastructure.Run;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.apache.jena.atlas.lib.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;
import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.classifiers.bayes.net.BIFReader;
import weka.classifiers.bayes.net.BayesNetGenerator;
import weka.classifiers.bayes.net.EditableBayesNet;
import weka.classifiers.functions.GaussianProcesses;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SMOreg;
import weka.classifiers.functions.SimpleLinearRegression;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.lazy.IBk;
import weka.classifiers.lazy.KStar;
import weka.classifiers.lazy.LWL;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.AdditiveRegression;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.CVParameterSelection;
import weka.classifiers.meta.ClassificationViaRegression;
import weka.classifiers.meta.CostSensitiveClassifier;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.meta.MultiClassClassifier;
import weka.classifiers.meta.MultiScheme;
import weka.classifiers.meta.RandomCommittee;
import weka.classifiers.meta.RandomSubSpace;
import weka.classifiers.meta.RegressionByDiscretization;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.Vote;
import weka.classifiers.rules.DecisionTable;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.M5Rules;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.rules.ZeroR;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.LMT;
import weka.classifiers.trees.M5P;
import weka.classifiers.trees.REPTree;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class SimpleClassificationWithLeaveOneOut {
    static Logger log = LoggerFactory.getLogger(SimpleClassificationWithLeaveOneOut.class);

    public static void main(String[] args) throws Exception {

        // 1. Learn on the training data for each system a classifier to find
        // out which system can answer which question

        // 1.1 load the questions and how good each system answers
        log.info("Load the questions and how good each system answers");

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        // 1.2 calculate the features per question and system
        log.info("Calculate the features per question and system");
        Analyzer analyzer = new Analyzer();
        Map<Integer, Map<String, Double>> classifierFmeasurePerQuestion = new HashMap<Integer, Map<String, Double>>();
        // TODO clean QALD_Loader.load(Dataset.QALD5_Test).size() according to
        // task 1, i.e.,
        // if (!goldSystemQuestion.outOfScope && !goldSystemQuestion.hybrid &&
        // !goldSystemQuestion.goldenAnswers.isEmpty()) {

        List<Question> questions = QALD_Loader.load(Dataset.QALD5_Test);
        // Create an empty training set per system
        File QALD5Logs = new File(classLoader.getResource("QALD-5_logs/").getFile());
        List<Run> runs = SearchBestQALDResult.searchBestRun(questions, QALD5Logs);

        List<Question> testQuestions = null;
        for (int leaveOut = 0; leaveOut < questions.size(); leaveOut++) {

            Map<Run, Instances> instancesPerRun = new HashMap<Run, Instances>();
            // leave out one question
            testQuestions = new ArrayList<Question>(questions);
            Question leaveOutQuestion = questions.get(leaveOut);
            testQuestions.remove(leaveOut);

            log.info("Start collection of training data for each system");

            for (Run run : runs) {
                Instances instances = new Instances("test_" + run.getName(), analyzer.fvWekaAttributes,
                        testQuestions.size());
                instances.setClass(analyzer.getClassAttribute());
                // TODO fix this, it calculates each feature per question
                // $runs.size() times
                for (Question q : testQuestions) {
                    // calculate features
                    Instance tmp = analyzer.analyze(q.languageToQuestion.get("en"));

                    // get f-measure of the system for this question
                    // check whether the system has that answer anyway
                    Double fmeasure = 0.0;
                    if (run.getMap().containsKey(q.languageToQuestion.get("en"))) {
                        fmeasure = run.getMap().get(q.languageToQuestion.get("en"));
                    }
                    // add to instances of the particular system
                    tmp.setValue((Attribute) analyzer.getClassAttribute(), fmeasure);
                    instances.add(tmp);
                }

                instancesPerRun.put(run, instances);
                log.debug(instances.toSummaryString());
            }

            // 2.3 use machine learning to train it
            log.info("Start machine learning");
            // CANNOT use the following classifiers GeneralRegression()
            // HoeffdingTree() InputMappedClassifier() LMTNode() NeuralNetwork()
            // Regression() RuleNode() RuleSetModel() SGD() SGDText()

            // all classifiers copied from docu
            // fails capabilities test J48, new LogisticBase(),

            Map<Run, List<Classifier>> run_classifiers = new HashMap<Run, List<Classifier>>();
            for (Run run : runs) {
                List<Classifier> classifierForEachRun = new ArrayList<Classifier>();
                List<Classifier> classifiers = Arrays.asList(new AdaBoostM1(), new AdditiveRegression(), new Bagging(),
                        new BayesNet(), new BayesNetGenerator(), new BIFReader(), new ClassificationViaRegression(),
                        new CostSensitiveClassifier(), new CVParameterSelection(), new DecisionStump(),
                        new DecisionTable(), new EditableBayesNet(), new FilteredClassifier(), new GaussianProcesses(),
                        new IBk(), new JRip(), new KStar(), new LinearRegression(), new LMT(), new Logistic(),
                        new LogitBoost(), new LWL(), new M5P(), new M5Rules(), new MultiClassClassifier(),
                        new MultilayerPerceptron(), new MultiScheme(), new NaiveBayes(), new NaiveBayesMultinomial(),
                        new NaiveBayesMultinomialUpdateable(), new NaiveBayesUpdateable(), new OneR(), new PART(),
                        new RandomCommittee(), new RandomForest(), new RandomSubSpace(), new RandomTree(),
                        new RegressionByDiscretization(), new REPTree(), new SimpleLinearRegression(),
                        new SimpleLogistic(), new SMO(), new SMOreg(), new Stacking(), new Vote(),
                        new VotedPerceptron(), new ZeroR());
                for (Classifier cModel : classifiers) {
                    Instances data = instancesPerRun.get(run);
                    if (cModel.getCapabilities().test(data)) {
                        // System.out.println(cModel.getClass().getSimpleName()
                        // + " added for " + run.getName());
                        cModel.buildClassifier(data);
                        // // Test the model
                        // Evaluation eval = new Evaluation(data);
                        // Random rand = new Random(1); // using seed = 1
                        // int folds = 10;
                        // eval.crossValidateModel(cModel, data, folds, rand);
                        classifierForEachRun.add(cModel);
                    }
                }
                run_classifiers.put(run, classifierForEachRun);
            }

            // 3. Use the classifier model to decide which system should answer
            // the current question and measure the performance
            log.info("Decision stage started");
            Map<String, List<Pair<Run, Double>>> classificationResult = new HashMap<String, List<Pair<Run, Double>>>();
            // logs the predicted class per classifier
            for (Run key_run : run_classifiers.keySet()) {
                String result = leaveOutQuestion.id + "\t" + key_run.getName() + "\t";
                for (Classifier classifier : run_classifiers.get(key_run)) {
                    Instance tmpInstance = analyzer.analyze(leaveOutQuestion.languageToQuestion.get("en"));
                    tmpInstance.setDataset(instancesPerRun.get(key_run));
                    double[] fDistribution = classifier.distributionForInstance(tmpInstance);
                    result += fDistribution[0] + "\t";
                    if (classificationResult.containsKey(classifier.getClass().getSimpleName())) {
                        // add result
                        classificationResult.get(classifier.getClass().getSimpleName())
                                .add(new Pair<Run, Double>(key_run, fDistribution[0]));
                    } else {
                        classificationResult.put(classifier.getClass().getSimpleName(),
                                new ArrayList<Pair<Run, Double>>(
                                        Arrays.asList(new Pair<Run, Double>(key_run, fDistribution[0]))));
                    }
                }
                log.debug(result);
            }

            // for each classifier find out which system is responsible and get
            // f-measure for the responsible system
            String result = "Result of the run: " + leaveOutQuestion.id + "\t";
            Map<String, Double> classifierToFMeasure = new HashMap<String, Double>();
            for (String classifierName : classificationResult.keySet()) {
                List<Pair<Run, Double>> results = classificationResult.get(classifierName);
                double max = Double.MIN_VALUE;
                Run maxRun = null;
                for (Pair<Run, Double> pair : results) {
                    if (pair.getRight() > max) {
                        maxRun = pair.getLeft();
                        max = pair.getRight();
                    }
                }
                // find the f-measure for this leaveOutQuestion and this system
                String leaveOutQuestionString = leaveOutQuestion.languageToQuestion.get("en");
                if (maxRun != null && maxRun.getMap().containsKey(leaveOutQuestionString)) {
                    double fmeasure = maxRun.getMap().get(leaveOutQuestionString);
                    result += fmeasure + "\t";
                    classifierToFMeasure.put(classifierName, fmeasure);
                } else {
                    // TODO I do not get why some maxRun are null
                    result += 0.0 + "\t";
                    classifierToFMeasure.put(classifierName, 0.0);
                }
            }
            classifierFmeasurePerQuestion.put(leaveOut, classifierToFMeasure);
            System.out.println(result);

        }
        // calculate average f-measure per classifier
        Map<String, Double> classifierToFMeasureSum = new HashMap<String, Double>();
        Map<String, Double> classifierToFMeasure = null;
        for (int question : classifierFmeasurePerQuestion.keySet()) {
            classifierToFMeasure = classifierFmeasurePerQuestion.get(question);
            for (String classifierName : classifierToFMeasure.keySet()) {
                if (classifierToFMeasureSum.containsKey(classifierName)) {
                    classifierToFMeasureSum.put(classifierName,
                            classifierToFMeasureSum.get(classifierName) + classifierToFMeasure.get(classifierName));
                } else {
                    classifierToFMeasureSum.put(classifierName, classifierToFMeasure.get(classifierName));
                }
            }
        }
        Map<String, Double> avgFmeasurePerClassifier = new HashMap<String, Double>();
        for (String classifierName : classifierToFMeasureSum.keySet()) {
            avgFmeasurePerClassifier.put(classifierName,
                    classifierToFMeasureSum.get(classifierName) / (double) questions.size());
        }

        // TODO write the following sysout to file

        for (String classifierName : avgFmeasurePerClassifier.keySet()) {
            System.out.println(classifierName + "\t" + avgFmeasurePerClassifier.get(classifierName));
        }
    }
}
