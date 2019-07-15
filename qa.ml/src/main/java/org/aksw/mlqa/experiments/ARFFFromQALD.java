package org.aksw.mlqa.experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.aksw.mlqa.analyzer.Analyzer;
import org.aksw.mlqa.utils.Utils;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.Lists;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class ARFFFromQALD {
	
	private ArrayList<String> systems = Lists.newArrayList("AskNow","QAKIS","TeBaQA", "QASystem", "QANSWER","GANSWER2");
	
	private static Logger log = LoggerFactory.getLogger(ARFFFromQALD.class);

	/***
	 * Creates an ARFF file from all questions inside the dataset. Saves the file inside the src/main/recources folder.
	 * @param dataset QALD dataset
	 * @param analyzer 
	 * @param file name of the ARFF file that is created
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void makeARFF(Dataset dataset, Analyzer analyzer, String file) throws JsonParseException, JsonMappingException, IOException {
		ArrayList<Attribute> fvfinal = analyzer.fvWekaAttributes;
		// create the slots for the systems/classes inside the feature vector
		ArrayList<String> test = Lists.newArrayList(); 
		for(String system: systems){
			test.add(system);
			fvfinal.add(0, new Attribute(system, Lists.newArrayList("0","1")));
		}

		// load the questions from the dataset
		List<IQuestion> correctAnswers = LoaderController.load(dataset);
		ArrayList<String> testQuestions = Lists.newArrayList();
		for(IQuestion q: correctAnswers) {
			testQuestions.add(q.getLanguageToQuestion().get("en"));
		}
		
		HashMap<String, ArrayList<Float>> fmeasures = new HashMap<String, ArrayList<Float>>();
		for(String system: systems){
			fmeasures.put(system, Utils.loadSystemF(system, correctAnswers.size()));
		}
		
		// create Instances
		Instances trainingSet = new Instances("training_classifier: -C " + systems.size() , fvfinal, testQuestions.size());
		log.debug("Start collection of training data for each system");
		for(int i = 0; i < testQuestions.size(); i++){
			Instance tmp = analyzer.analyze(testQuestions.get(i));
			for(int j = 0; j < systems.size(); j++){
				//if F-measure > 0, target will be 1, else 0
				if(new Float(fmeasures.get((systems.get(systems.size() -1  - j))).get(i)) > 0){
					tmp.setValue(j, 1);
				} else {
					tmp.setValue(j, 0);
				}
			}
			trainingSet.add(tmp);
		}
		log.debug(trainingSet.toString());
		
		try (FileWriter f = new FileWriter("./src/main/resources/" + file)) {
			f.write(trainingSet.toString());
		} catch (IOException e) {
			log.debug("writing failed.");
			e.printStackTrace();
		}				
	}
	
	public ArrayList<String> getSystems() {
		return systems;
	}

	public void setSystems(ArrayList<String> systems) {
		this.systems = systems;
	}
}
