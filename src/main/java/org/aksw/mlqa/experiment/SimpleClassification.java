package org.aksw.mlqa.experiment;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;

import org.aksw.mlqa.analyzer.Analyzer;
import org.aksw.mlqa.datastructure.Run;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibLINEAR;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.NumericToNominal;
import weka.filters.unsupervised.attribute.StringToNominal;

public class SimpleClassification {
	static Logger log = LoggerFactory.getLogger(SimpleClassification.class);

	public static void main(String[] args) throws Exception {

		// 1. Learn on the training data for each system a classifier to find
		// out which system can answer which question

		// 1.1 load the questions and how good each system answers
		log.debug("Load the questions and how good each system answers");
		List<Question> trainQuestions = QALD_Loader.load(Dataset.QALD5_Train);

		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		File QALD5Logs = new File(classLoader.getResource("QALD-5_logs/").getFile());

		List<Run> runs = SearchBestQALDResult.searchBestRun(trainQuestions, QALD5Logs);

		// 1.2 calculate the features per question and system
		log.debug("Calculate the features per question and system");

		Analyzer analyzer = new Analyzer();
		// Create an empty training set per system
		Map<Run, Instances> instancesPerRun = new HashMap<Run, Instances>();
		for (Run run : runs) {
			Instances isTrainingSet = new Instances("training_" + run.getName(), analyzer.fvWekaAttributes, trainQuestions.size());
			// set class attribute
			isTrainingSet.setClass(analyzer.getClassAttribute());
			instancesPerRun.put(run, isTrainingSet);
		}

		log.debug("Start collection of training data for each system");

		for (Run run : runs) {
			Instances instances = instancesPerRun.get(run);
			// TODO fix this, it calculates each feature per question
			// $runs.size() times
			for (Question q : trainQuestions) {
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
			// transforms the class attribute (which is a double) into a nominal
			// attribute

//			NumericToNominal ntn = new NumericToNominal();
//			ntn.setAttributeIndices("last");
//			ntn.setInputFormat(instances);
//			instances = Filter.useFilter(instances, ntn);

			// // transforms entity types to nominals
			// StringToNominal stringToNominal;
			// Instances filteredTrainingSet = instances;
			//
			// try {
			// for (int attIndex = 0; attIndex < instances.numAttributes() - 1;
			// attIndex++) {
			// if (instances.attribute(attIndex).isString()) {
			// stringToNominal = new StringToNominal();
			// stringToNominal.setInputFormat(filteredTrainingSet);
			// filteredTrainingSet = Filter.useFilter(filteredTrainingSet,
			// stringToNominal);
			// }
			// }
			// } catch (Exception ex) {
			// throw new RuntimeException("String to nominal conversion failed",
			// ex);
			// }

			instancesPerRun.put(run, instances);
			System.out.println(instances.toString());
			log.info(instances.toSummaryString());
		}
		// TODO since many systems submit only a subset of questions, take all
		// submitted logs together and optimize best training set for each
		// system by remembering the best f-measure for each question. Hope is,
		// we get less sparse instances

		// 2.3 use machine learning to train it
		log.debug("Start machine learning");
		// iterate the classifiers here
		Map<Run, Classifier> classifierPerRun = new HashMap<Run, Classifier>();
		for (Run run : runs) {
			// TODO add more classifiers (see
			// https://github.com/AKSW/fox/blob/master/src%2Fmain%2Fjava%2Forg%2Faksw%2Ffox%2Fnerlearner%2FFoxClassifierFactory.java)
			// Create a naïve bayes classifier

			Classifier cModel = (Classifier) new J48();
			cModel.buildClassifier(instancesPerRun.get(run));

			//TODO  // Test the model
//			 Evaluation eTest = new Evaluation(isTrainingSet);
//			 eTest.evaluateModel(cModel, isTestingSet);
//
//			The evaluation module can output a bunch of statistics:
//			 // Print the result à la Weka explorer:
//			 String strSummary = eTest.toSummaryString();
//			 System.out.println(strSummary);
//			 
//			 // Get the confusion matrix
//			 double[][] cmMatrix = eTest.confusionMatrix();
//			 
			classifierPerRun.put(run, cModel);
		}
		// 3. Use the classifier model to decide which system should answer the
		// current question and measure the performance
		List<Question> testQuestions = QALD_Loader.load(Dataset.QALD5_Test);

		for (Question q : testQuestions) {
			// calculate features
			Instance tmpInstance = analyzer.analyze(q.languageToQuestion.get("en"));
			System.out.println(tmpInstance.toString());
			// decide which system to use
			String result = q.id + "\t";
			for (Run key : classifierPerRun.keySet()) {
				tmpInstance.setDataset(instancesPerRun.get(key));
				double[] fDistribution = classifierPerRun.get(key).distributionForInstance(tmpInstance);
				result += fDistribution[0] + "\t" + fDistribution[1] + "\t";
			}
			log.info(result);

			// TODO calculate f-measure
		}
	}
}
