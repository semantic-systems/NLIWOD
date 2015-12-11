package org.aksw.mlqa.experiment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.aksw.mlqa.analyzer.Analyzer;
import org.aksw.mlqa.datastructure.Run;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
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
		log.debug("Load the questions and how good each system answers");
		List<Question> testQuestions = QALD_Loader.load(Dataset.QALD5_Test);

		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		File QALD5Logs = new File(classLoader.getResource("QALD-5_logs/").getFile());

		List<Run> runs = SearchBestQALDResult.searchBestRun(testQuestions, QALD5Logs);

		// 1.2 calculate the features per question and system
		log.debug("Calculate the features per question and system");
		// TODO do the leave one out loop
		Analyzer analyzer = new Analyzer();
		// Create an empty training set per system
		Map<Run, Instances> instancesPerRun = new HashMap<Run, Instances>();
		for (Run run : runs) {
			Instances isTrainingSet = new Instances("test_" + run.getName(), analyzer.fvWekaAttributes, testQuestions.size());
			// set class attribute
			isTrainingSet.setClass(analyzer.getClassAttribute());
			instancesPerRun.put(run, isTrainingSet);
		}

		log.debug("Start collection of training data for each system");

		for (Run run : runs) {
			Instances instances = instancesPerRun.get(run);
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
			log.info(instances.toSummaryString());
		}

		// 2.3 use machine learning to train it
		log.debug("Start machine learning");
		// CANNOT use the following classifiers GeneralRegression()
		// HoeffdingTree() InputMappedClassifier() LMTNode() NeuralNetwork()
		// Regression() RuleNode() RuleSetModel() SGD() SGDText()

		// all classifiers copied from docu
		// fails capabilities test J48, new LogisticBase(),
		List<Classifier> classifiers = new ArrayList<Classifier>(Arrays.asList(new AdaBoostM1(), new AdditiveRegression(), new Bagging(), new BayesNet(), new BayesNetGenerator(), new BIFReader(),
		        new ClassificationViaRegression(), new CostSensitiveClassifier(), new CVParameterSelection(), new DecisionStump(), new DecisionTable(), new EditableBayesNet(),
		        new FilteredClassifier(), new GaussianProcesses(), new IBk(), new JRip(), new KStar(), new LinearRegression(), new LMT(), new Logistic(), new LogitBoost(), new LWL(), new M5P(),
		        new M5Rules(), new MultiClassClassifier(), new MultilayerPerceptron(), new MultiScheme(), new NaiveBayes(), new NaiveBayesMultinomial(), new NaiveBayesMultinomialUpdateable(),
		        new NaiveBayesUpdateable(), new OneR(), new PART(), new RandomCommittee(), new RandomForest(), new RandomSubSpace(), new RandomTree(), new RegressionByDiscretization(), new REPTree(),
		        new SimpleLinearRegression(), new SimpleLogistic(), new SMO(), new SMOreg(), new Stacking(), new Vote(), new VotedPerceptron(), new ZeroR()));

		for (Classifier cModel : classifiers) {
			System.out.print(cModel.getClass().getName());
			for (Run run : runs) {
				Instances data = instancesPerRun.get(run);
				if (cModel.getCapabilities().test(data)) {
					cModel.buildClassifier(data);
					// Test the model
					Evaluation eval = new Evaluation(data);
					Random rand = new Random(1); // using seed = 1
					int folds = 10;
					eval.crossValidateModel(cModel, data, folds, rand);
					System.out.print("\t" + eval.relativeAbsoluteError());
				}
			}
			System.out.println();

		}
		// 3. Use the classifier model to decide which system should answer the
		// current question and measure the performance
		// List<Question> testQuestions = QALD_Loader.load(Dataset.QALD5_Test);
		//
		// for (Question q : testQuestions) {
		// // calculate features
		// Instance tmpInstance =
		// analyzer.analyze(q.languageToQuestion.get("en"));
		// System.out.println(tmpInstance.toString());
		// // decide which system to use
		// String result = q.id + "\t";
		// for (Run key : classifierPerRun.keySet()) {
		// tmpInstance.setDataset(instancesPerRun.get(key));
		// double[] fDistribution =
		// classifierPerRun.get(key).distributionForInstance(tmpInstance);
		// result += fDistribution[0] + "\t" + fDistribution[1] + "\t";
		// }
		// log.info(result);
		//
		// // TODO calculate f-measure
		// }
	}
}
