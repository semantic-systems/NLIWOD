package org.aksw.mlqa.experiments;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.aksw.mlqa.utils.Utils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.collect.Lists;

import weka.classifiers.Classifier;
import weka.core.Debug.Random;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;


/***
 * The purpose of this Class is to do k-fold cross-validation of our classification algorithm. 
 * @author Lukas
 *
 */
public class CrossValidation {
	
	private ArrayList<String> systems = Lists.newArrayList("AskNow", "QAKIS", "TebaQA", "QASystem", "QANSWER", "GANSWER2");

	/***
	 * Performs a k-fold cross-validation for the given classifier, fold count, and ARFF file containing a list of instances.
	 * @param classifier any multi-label classifier from the MEKA library
	 * @param folds set to -1 for leave-one-out cross-validation
	 * @param arff ARFF file located in src/main/resources
	 * @param dataset the data set the instances from the arff file are based on
	 * @throws Exception
	 */
	public void cVModel(Classifier classifier, int folds, String arffFile) throws Exception {
		int classesAmount = systems.size();
		Path datapath= Paths.get("./src/main/resources/" + arffFile);
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arffReader = new ArffReader(reader);
		Instances data = arffReader.getData();
		data.setClassIndex(classesAmount);

		HashMap<String, ArrayList<Float>> precisions = new HashMap<String, ArrayList<Float>>();
		HashMap<String, ArrayList<Float>> recalls = new HashMap<String, ArrayList<Float>>();
		for(String system: systems){
			precisions.put(system, Utils.loadSystemP(system, data.size()));
		}
		for(String system: systems){
			recalls.put(system, Utils.loadSystemR(system, data.size()));
		}
		
		int seed = 135;
		Random rand = new Random(seed);
		Instances randData = new Instances(data);		
		randData.randomize(rand);
		
		float cv_ave_f = 0;		
		
		//leave-one-out cross-validation
		if(folds == -1) folds = data.size();
		
		double[] boxplot = new double[folds];
		for(int n=0; n < folds; n++){
		    Instances train = randData.trainCV(folds,  n);
		    Instances test = randData.testCV(folds,  n);

			classifier.buildClassifier(train);
			
			float ave_p = 0;
			float ave_r = 0;
	
			for(int j = 0; j < test.size(); j++){
				Instance ins = test.get(j);
				int k = 0; 
				for(int l=0; l < data.size(); l++){
					Instance tmp = data.get(l);
					if(tmp.toString().equals(ins.toString())){
						k = l;
					}
				}		
				double[] confidences = classifier.distributionForInstance(ins);
				int argmax = -1;
				double max = -1;
					for(int i = 0; i < classesAmount; i++){
						if(confidences[i]>max){
							max = confidences[i];
							argmax = i;
						}
				}
				String sys2ask = systems.get(systems.size() - argmax -1);
				ave_p += precisions.get((sys2ask)).get(k);				
				ave_r += recalls.get((sys2ask)).get(k);
			}
			
			double p = ave_p/test.size();
			double r = ave_r/test.size();
			double fmeasure = 0;
			if(p>0&&r>0){fmeasure = 2*p*r/(p + r);}
			
			System.out.println("macro F on fold " + n + ": " + fmeasure);
			cv_ave_f += fmeasure/folds;
			boxplot[n] = fmeasure;
		}
		
		System.out.println("macro F average: " + cv_ave_f);
		System.out.println('\n');
		
		//only display boxplot data for 10 folds
		if(folds == 10 ) {
			System.out.println("Data for the boxplot:");
			DescriptiveStatistics da = new DescriptiveStatistics(boxplot);
			System.out.println("lower whisker=" + da.getMin()+ ",");
			System.out.println("lower quartile=" + da.getPercentile(25)+ ",");
			System.out.println("median=" + da.getPercentile(50)+ ",");
			System.out.println("upper quartile=" + da.getPercentile(75)+ ",");
			System.out.println("upper whisker=" + da.getMax()+ ",");
			System.out.println("average=" + da.getMean() + ",");
		}
	}

	public ArrayList<String> getSystems() {
		return systems;
	}

	public void setSystems(ArrayList<String> systems) {
		this.systems = systems;
	}
}