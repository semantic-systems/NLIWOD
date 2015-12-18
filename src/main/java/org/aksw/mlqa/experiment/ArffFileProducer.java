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

import org.aksw.mlqa.analyzer.StaticManualAnalyzer;
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
import weka.core.AttributeCopyHelper;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ArffFileProducer {
	static Logger log = LoggerFactory.getLogger(ArffFileProducer.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {

		ClassLoader classLoader = ClassLoader.getSystemClassLoader();

		StaticManualAnalyzer analyzer = new StaticManualAnalyzer();

		List<Question> questions = QALD_Loader.load(Dataset.QALD5_Test);
		// Create an empty training set per system
		File QALD5Logs = new File(classLoader.getResource("QALD-5_logs/").getFile());
		questions = SearchBestQALDResult.filterQuestions(questions);
		List<Run> runs = SearchBestQALDResult.searchBestRun(questions, QALD5Logs);

		for (Run run : runs) {
			Instances instances = new Instances("test_" + run.getName(), analyzer.fvWekaAttributes, questions.size());
			for (Question q : questions) {
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
			System.out.println(instances.toString());
		}
	}
}
