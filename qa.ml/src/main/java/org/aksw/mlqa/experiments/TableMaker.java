package org.aksw.mlqa.experiments;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.aksw.mlqa.utils.Utils;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;

import com.google.common.collect.Lists;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/*
 * Creates the large Table from the thesis. Additionally, computes macro values of all systems.
 */

public class TableMaker {
	
	private ArrayList<String> systems = Lists.newArrayList("AskNow","QAKIS","TeBaQA", "QASystem", "QANSWER","GANSWER2");
	
	/***
	 * Trains the given classifier on all instances from the dataset. Then computes the large table from the thesis, with the f-measures 
	 * for every system.
	 * Additionally, computes the macro values of all systems.
	 * @param dataset dataset of questions which the instances in the ARFF file represent
	 * @param classifier any multi-label classifier from the MEKA library
	 * @param arffFile ARFF file located in src/main/resources
	 * @throws Exception
	 */
	public void makeTable(Dataset dataset, Classifier classifier, String arffFile) throws Exception {
		int classesAmount = systems.size();
		Path datapath= Paths.get("./src/main/resources/" + arffFile);
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(classesAmount);
		
		//train the classifier on the instances from the ARFF file
		classifier.buildClassifier(data);
		
		List<IQuestion> correctAnswers = LoaderController.load(dataset);
		ArrayList<String> testQuestions = Lists.newArrayList();
		for(IQuestion q: correctAnswers) {
			testQuestions.add(q.getLanguageToQuestion().get("en"));
		}
		
		HashMap<String, ArrayList<Float>> precisions = new HashMap<String, ArrayList<Float>>();
		HashMap<String, ArrayList<Float>> recalls = new HashMap<String, ArrayList<Float>>();
		for(String system: systems){
			precisions.put(system, Utils.loadSystemP(system, data.size()));
		}
		for(String system: systems){
			recalls.put(system, Utils.loadSystemR(system, data.size()));
		}

		double avef = 0;
		double aver = 0;
		double avep = 0;
		double[] systemavef = new double[classesAmount+1];
		double[] systemaver = new double[classesAmount+1];
		double[] systemavep = new double[classesAmount+1];

		// iterate over all instances and print the F-measure of each system for the instances
		for(int i=0; i<data.size(); i++){
			
			String tmp = "";
			tmp += i +"\t &" + testQuestions.get(i);
			float bestf = 0;
			float bestr = 0;
			float bestp = 0;
			
			// find measures of the optimal system
			for(String system: systems){
				float p = precisions.get((system)).get(i);				
				float r = recalls.get((system)).get(i);
				float f = 0;
				if( !(p==0&&r==0) ) {
					f = 2*p*r/(p+r);
				}
				
				if(f > bestf) bestf = f;				
				if(r > bestr) bestr = r;			
				if(p > bestp) bestp = p;
								
				tmp += "\t &" + Math.floor(f * 100) / 100;
				systemavef[systems.indexOf(system)] += f/data.size();
				systemaver[systems.indexOf(system)] += r/data.size();
				systemavep[systems.indexOf(system)] += p/data.size();
			}

			systemavef[classesAmount] += bestf/data.size();
			systemaver[classesAmount] += bestr/data.size();
			systemavep[classesAmount] += bestp/data.size();
			tmp += "\t &" + Math.floor(bestf * 100) / 100;
			
			// get system prediction from the classifier
			double[] confidences = classifier.distributionForInstance(data.get(i));
			int argmax = -1;
			double max = -1;
				for(int j = 0; j < classesAmount; j++) {
					if(confidences[j]>max){
						max = confidences[j];
						argmax = j;
				}
			}
			
			String sys2ask = systems.get(systems.size() - argmax -1);
			float systemp = precisions.get((sys2ask)).get(i);			
			float systemr = recalls.get((sys2ask)).get(i);	
			float systemf = 0;
			
			if(!( systemp == 0 && systemr == 0)){
				systemf = 2*systemp*systemr/(systemp+systemr);
			}
			avef += systemf;
			avep += systemp;
			aver += systemr;
			tmp += "\t &" + Math.floor(systemf * 100) / 100;

			tmp += "\\\\";
			System.out.println(tmp);
		}
		
		System.out.println("\n\n\nAskNow, QAKiS, TeBaQA, QASystem, QANSWER, GANSWER2, Optimal");
		System.out.println("F1:        " + Arrays.toString(systemavef));
		System.out.println("Recall:    " + Arrays.toString(systemaver));
		System.out.println("Precision: " + Arrays.toString(systemavep));
		System.out.println();
		System.out.println("Metasystem F1: " + avef/data.size());
		System.out.println("Metasystem Recall: " + aver/data.size());
		System.out.println("Metasystem Precision: " + avep/data.size());
	}
	
	public ArrayList<String> getSystems() {
		return systems;
	}

	public void setSystems(ArrayList<String> systems) {
		this.systems = systems;
	}
}