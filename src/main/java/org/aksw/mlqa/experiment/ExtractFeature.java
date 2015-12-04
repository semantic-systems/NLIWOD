package org.aksw.mlqa.experiment;

import java.io.InputStream;
import java.util.List;

import org.aksw.mlqa.analyzer.Analyzer;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.QALD_Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Instance;
import weka.core.Instances;

public class ExtractFeature {
	static Logger log = LoggerFactory.getLogger(ExtractFeature.class);

	public static void main(String[] args) {
		InputStream file = ClassLoader.getSystemResourceAsStream("QALD-5/qald-5_test.xml");
		List<Question> questions = QALD_Loader.load(file);

		Analyzer analyzer = new Analyzer();
		// Create an empty training set
		Instances isTrainingSet = new Instances("training", analyzer.fvWekaAttributes, questions.size());

		for (Question q : questions) {
			// calculate features
			Instance tmp = analyzer.analyze(q.languageToQuestion.get("en"));
			// output feature vector
			log.debug(tmp.toString());

			// add to instances
			isTrainingSet.add(tmp);
		}

	}
}
