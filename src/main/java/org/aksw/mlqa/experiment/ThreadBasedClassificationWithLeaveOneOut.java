package org.aksw.mlqa.experiment;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.aksw.mlqa.analyzer.StaticManualAnalyzer;
import org.aksw.mlqa.datastructure.Run;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.aksw.simba.topicmodeling.concurrent.overseers.pool.DefeatableOverseer;
import org.aksw.simba.topicmodeling.concurrent.overseers.pool.ExecutorBasedOverseer;
import org.aksw.simba.topicmodeling.concurrent.tasks.Task;
import org.aksw.simba.topicmodeling.concurrent.tasks.TaskObserver;
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
import weka.core.AttributeCopyHelper;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ThreadBasedClassificationWithLeaveOneOut implements TaskObserver {
    static Logger log = LoggerFactory.getLogger(ThreadBasedClassificationWithLeaveOneOut.class);

    public static final int NUMBER_OF_WORKERS = 20;

    public static void main(String[] args) throws Exception {
        ThreadBasedClassificationWithLeaveOneOut experiment = new ThreadBasedClassificationWithLeaveOneOut();
        experiment.run();
    }

    private Semaphore waitMutex = new Semaphore(0);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void run() throws Exception {
        // 1. Learn on the training data for each system a classifier to find
        // out which system can answer which question

        // 1.1 load the questions and how good each system answers
        log.info("Load the questions and how good each system answers");

        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        // 1.2 calculate the features per question and system
        log.info("Calculate the features per question and system");
        // Analyzer analyzer = new Analyzer();
        StaticManualAnalyzer analyzer = new StaticManualAnalyzer();
        // TODO clean QALD_Loader.load(Dataset.QALD5_Test).size() according to
        // task 1, i.e.,
        // if (!goldSystemQuestion.outOfScope && !goldSystemQuestion.hybrid &&
        // !goldSystemQuestion.goldenAnswers.isEmpty()) {

        List<Question> questions = QALD_Loader.load(Dataset.QALD5_Test);
        // Create an empty training set per system
        File QALD5Logs = new File("QALD-5_logs");
        questions = SearchBestQALDResult.filterQuestions(questions);
        List<Run> runs = SearchBestQALDResult.searchBestRun(questions, QALD5Logs);

        // analyze queries
        Instance origInstances[] = new Instance[questions.size()];
        for (int i = 0; i < origInstances.length; ++i) {
            origInstances[i] = analyzer.analyze(questions.get(i).languageToQuestion.get("en"));
        }

        // note that the last attribute is the f-measure
        FastVector attributes = analyzer.getAttributes();
        int numberOfChoosableAttributes = attributes.size() - 1;
        int numberOfAttCombinations = 1 << numberOfChoosableAttributes;
        Map resultMaps[] = new Map[numberOfAttCombinations];
        DefeatableOverseer overseer = new ExecutorBasedOverseer(NUMBER_OF_WORKERS);
        overseer.addObserver(this);
        for (int chosenAttributes = 1; chosenAttributes < numberOfAttCombinations; ++chosenAttributes) {
            overseer.startTask(new AttributeCombinationTask(chosenAttributes, runs, questions, attributes,
                    analyzer.getClassAttribute(), origInstances, resultMaps));
        }
        log.info("Main thread is waiting for the single task to be finished...");
        waitMutex.acquire(numberOfAttCombinations - 1);
        log.info("Main thread finished waiting.");

        Set<String> classifierNames = new HashSet<String>();
        for (int i = 1; i < resultMaps.length; ++i) {
            classifierNames.addAll(resultMaps[i].keySet());
        }

        PrintStream pStream = null;
        double f1, maxF1 = 0;
        int maxCombo = 0;
        try {
            pStream = new PrintStream("result.tsv");
            // write head line
            pStream.print("id");
            for (int i = 0; i < numberOfChoosableAttributes; ++i) {
                pStream.print('\t');
                pStream.print(((Attribute) attributes.elementAt(i)).name());
            }
            for (String classifierName : classifierNames) {
                pStream.print('\t');
                pStream.print(classifierName);
            }
            pStream.println();
            // print the results
            Map<String, Double> avgFmeasurePerClassifier;
            for (int i = 1; i < resultMaps.length; ++i) {
                avgFmeasurePerClassifier = resultMaps[i];
                pStream.print(i);
                for (int j = 0; j < numberOfChoosableAttributes; ++j) {
                    pStream.print('\t');
                    pStream.print((i & (1 << j)) > 0 ? 1 : 0);
                }
                for (String classifierName : classifierNames) {
                    pStream.print('\t');
                    if (avgFmeasurePerClassifier.containsKey(classifierName)) {
                        f1 = avgFmeasurePerClassifier.get(classifierName);
                        pStream.print(f1);
                        if (f1 > maxF1) {
                            maxF1 = f1;
                            maxCombo = i;
                        }
                    } else {
                        log.warn("There is no result for a classifier \"" + classifierName + "\" for the run " + i
                                + ".");
                        pStream.print("null");
                    }
                }
                pStream.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pStream != null) {
                try {
                    pStream.close();
                } catch (Exception e) {
                }
            }
        }
        log.info("Best result is combination #" + maxCombo + " with " + maxF1);
        overseer.shutdown();
    }

    @Override
    public void reportTaskFinished(Task task) {
        waitMutex.release();
    }

    @Override
    public void reportTaskThrowedException(Task task, Throwable t) {
        log.error("Got an exception from task \"" + task.getId() + "\".", t);
        waitMutex.release();
    }

    @SuppressWarnings("rawtypes")
    protected static class AttributeCombinationTask implements Task {

        private int chosenAttributes;
        private List<Run> runs;
        private List<Question> questions;
        private FastVector attributes;
        private Attribute originalClassAttribute;
        private Instance origInstances[];
        private Map resultMaps[];

        public AttributeCombinationTask(int chosenAttributes, List<Run> runs, List<Question> questions,
                FastVector attributes, Attribute originalClassAttribute, Instance[] origInstances, Map[] resultMaps) {
            this.chosenAttributes = chosenAttributes;
            this.runs = runs;
            this.questions = questions;
            this.attributes = attributes;
            this.originalClassAttribute = originalClassAttribute;
            this.origInstances = origInstances;
            this.resultMaps = resultMaps;
        }

        public void run() {
            int numberOfChoosableAttributes = attributes.size() - 1;

            // Generate Attribute instances with the correct indexes
            Map<String, Attribute> newAttributeInstances = new HashMap<String, Attribute>();
            FastVector newAttributeVector = new FastVector();
            Attribute attribute, newAttribute, classAttribute;
            for (int j = 0; j < numberOfChoosableAttributes; ++j) {
                attribute = (Attribute) attributes.elementAt(j);
                if ((chosenAttributes & (1 << j)) > 0) {
                    newAttribute = AttributeCopyHelper.copy(attribute, newAttributeVector.size());
                    newAttributeInstances.put(newAttribute.name(), newAttribute);
                    newAttributeVector.addElement(newAttribute);
                }
            }
            classAttribute = new Attribute(originalClassAttribute.name(), newAttributeVector.size());
            newAttributeVector.addElement(classAttribute);

            Map<Run, Instance[]> instancesWithAttributesPerRun = new HashMap<Run, Instance[]>();
            Instance instancesWithAttributes[];
            Question question;
            for (Run run : runs) {
                instancesWithAttributes = new Instance[questions.size()];
                for (int i = 0; i < instancesWithAttributes.length; ++i) {
                    question = questions.get(i);
                    instancesWithAttributes[i] = new Instance(Integer.bitCount(chosenAttributes) + 1);
                    for (int j = 0; j < numberOfChoosableAttributes; ++j) {
                        attribute = (Attribute) attributes.elementAt(j);
                        if ((chosenAttributes & (1 << j)) > 0) {
                            if (attribute.isNumeric()) {
                                instancesWithAttributes[i].setValue(newAttributeInstances.get(attribute.name()),
                                        origInstances[i].value(attribute));
                            } else {
                                instancesWithAttributes[i].setValue(newAttributeInstances.get(attribute.name()),
                                        origInstances[i].stringValue(attribute));
                            }
                        }
                    }
                    // get f-measure of the system for this question
                    // check whether the system has that answer anyway
                    Double fmeasure = 0.0;
                    if (run.getMap().containsKey(question.languageToQuestion.get("en"))) {
                        fmeasure = run.getMap().get(question.languageToQuestion.get("en"));
                    }
                    // add to instances of the particular system
                    instancesWithAttributes[i].setValue(classAttribute, fmeasure);
                }
                instancesWithAttributesPerRun.put(run, instancesWithAttributes);
            }

            List<Question> testQuestions;
            Map<Integer, Map<String, Double>> classifierFmeasurePerQuestion = new HashMap<Integer, Map<String, Double>>();
            for (int leaveOut = 0; leaveOut < questions.size(); leaveOut++) {

                Map<Run, Instances> instancesPerRun = new HashMap<Run, Instances>();
                // leave out one question
                testQuestions = new ArrayList<Question>(questions);
                Question leaveOutQuestion = questions.get(leaveOut);
                testQuestions.remove(leaveOut);

                // log.info("Start collection of training data for each
                // system");

                // we have to create a smaller instance with less attributes.
                for (Run run : runs) {
                    instancesWithAttributes = instancesWithAttributesPerRun.get(run);
                    Instances instances = new Instances("test_" + run.getName(), newAttributeVector,
                            testQuestions.size());
                    instances.setClass(classAttribute);

                    for (int i = 0; i < instancesWithAttributes.length; ++i) {
                        if (i != leaveOut) {
                            instances.add(instancesWithAttributes[i]);
                        }
                    }

                    instancesPerRun.put(run, instances);
                    log.debug(instances.toSummaryString());
                }

                // 2.3 use machine learning to train it
                // log.info("Start machine learning");
                // CANNOT use the following classifiers GeneralRegression()
                // HoeffdingTree() InputMappedClassifier() LMTNode()
                // NeuralNetwork()
                // Regression() RuleNode() RuleSetModel() SGD() SGDText()

                // all classifiers copied from docu
                // fails capabilities test J48, new LogisticBase(),

                Map<Run, List<Classifier>> run_classifiers = new HashMap<Run, List<Classifier>>();
                for (Run run : runs) {
                    List<Classifier> classifierForEachRun = new ArrayList<Classifier>();
                    List<Classifier> classifiers = Arrays.asList(new AdaBoostM1(), new AdditiveRegression(),
                            new Bagging(), new BayesNet(), new BayesNetGenerator(), new BIFReader(),
                            new ClassificationViaRegression(), new CostSensitiveClassifier(),
                            new CVParameterSelection(), new DecisionStump(), new DecisionTable(),
                            new EditableBayesNet(), new FilteredClassifier(), new GaussianProcesses(), new IBk(),
                            new JRip(), new KStar(), new LinearRegression(), new LMT(), new Logistic(),
                            new LogitBoost(), new LWL(), new M5P(), new M5Rules(), new MultiClassClassifier(),
                            new MultilayerPerceptron(), new MultiScheme(), new NaiveBayes(),
                            new NaiveBayesMultinomial(), new NaiveBayesMultinomialUpdateable(),
                            new NaiveBayesUpdateable(), new OneR(), new PART(), new RandomCommittee(),
                            new RandomForest(), new RandomSubSpace(), new RandomTree(),
                            new RegressionByDiscretization(), new REPTree(), new SimpleLinearRegression(),
                            new SimpleLogistic(), new SMO(), new SMOreg(), new Stacking(), new Vote(),
                            new VotedPerceptron(), new ZeroR());
                    for (Classifier cModel : classifiers) {
                        Instances data = instancesPerRun.get(run);
                        if (cModel.getCapabilities().test(data)) {
                            // System.out.println(cModel.getClass().getSimpleName()
                            // + " added for " + run.getName());
                            try {
                                cModel.buildClassifier(data);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                            // // Test the model
                            // Evaluation eval = new Evaluation(data);
                            // Random rand = new Random(1); // using seed = 1
                            // int folds = 10;
                            // eval.crossValidateModel(cModel, data, folds,
                            // rand);
                            classifierForEachRun.add(cModel);
                        }
                    }
                    run_classifiers.put(run, classifierForEachRun);
                }

                // 3. Use the classifier model to decide which system should
                // answer
                // the current question and measure the performance
                Map<String, List<Pair<Run, Double>>> classificationResult = new HashMap<String, List<Pair<Run, Double>>>();
                // logs the predicted class per classifier
                double[] fDistribution;
                for (Run key_run : run_classifiers.keySet()) {
                    String result = leaveOutQuestion.id + "\t" + key_run.getName() + "\t";
                    for (Classifier classifier : run_classifiers.get(key_run)) {
                        Instance tmpInstance = new Instance(instancesWithAttributesPerRun.get(key_run)[leaveOut]);
                        tmpInstance.setDataset(instancesPerRun.get(key_run));
                        try {
                            fDistribution = classifier.distributionForInstance(tmpInstance);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
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

                // for each classifier find out which system is responsible and
                // get
                // f-measure for the responsible system
                String result = "attributes: " + chosenAttributes + "\trun: " + leaveOutQuestion.id + "\t";
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
                    // find the f-measure for this leaveOutQuestion and this
                    // system
                    String leaveOutQuestionString = leaveOutQuestion.languageToQuestion.get("en");
                    if (maxRun != null) {
                        // Make sure that the system answered this query
                        if (maxRun.getMap().containsKey(leaveOutQuestionString)) {
                            double fmeasure = maxRun.getMap().get(leaveOutQuestionString);
                            result += maxRun.getName() + ": " + fmeasure + "\t";
                            classifierToFMeasure.put(classifierName, fmeasure);
                        } else {
                            result += maxRun.getName() + ": 0.0\t";
                            classifierToFMeasure.put(classifierName, 0.0);
                        }
                    } else {
                        result += "null: 0.0\t";
                        classifierToFMeasure.put(classifierName, 0.0);
                    }
                }
                classifierFmeasurePerQuestion.put(leaveOut, classifierToFMeasure);
                // System.out.println(result);
                log.info(result);
            }
            // calculate average f-measure per classifier
            Map<String, Double> classifierToFMeasureSum = new HashMap<String, Double>();
            Map<String, Double> classifierToFMeasure = null;
            for (int questionId : classifierFmeasurePerQuestion.keySet()) {
                classifierToFMeasure = classifierFmeasurePerQuestion.get(questionId);
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

            for (String classifierName : avgFmeasurePerClassifier.keySet()) {
                log.info("attributes: " + chosenAttributes + "\t" + classifierName + "\t"
                        + avgFmeasurePerClassifier.get(classifierName));
            }

            resultMaps[chosenAttributes] = avgFmeasurePerClassifier;
        }

        @Override
        public String getId() {
            return "AttCombination=" + chosenAttributes;
        }

        @Override
        public String getProgress() {
            return null;
        }
    }
}
