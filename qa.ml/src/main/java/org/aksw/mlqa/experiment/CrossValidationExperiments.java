package org.aksw.mlqa.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import com.google.common.collect.Lists;

import meka.classifiers.multilabel.RT;
import weka.core.Debug.Random;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/*
 * The purpose of this Class is to do k-fold Cross Validation of our classification algorithm on the QALD6 benchmark data. 
 */

public class CrossValidationExperiments {
	// private static Logger log = LoggerFactory.getLogger(CrossValidationExperiments.class);
	
	
	public static void main(String[] args) throws Exception {		

		Path datapath= Paths.get("./src/main/resources/Qald6Logs.arff");
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(6);
		
		ArrayList<String> systems = Lists.newArrayList("KWGAnswer", "NbFramework", "PersianQA", "SemGraphQA", "UIQA_withoutManualEntries", "UTQA_English" );


		int seed = 133;
		// Change to 100 for leave-one-out CV
		int folds = 10;
		
		Random rand = new Random(seed);
		Instances randData = new Instances(data);
		randData.randomize(rand);
		
		float cv_ave_f = 0;
		
		for(int n=0; n < folds; n++){
		    Instances train = randData.trainCV(folds,  n);
		    Instances test = randData.testCV(folds,  n);
		    
		    //Change to the Classifier of your choice
			RT Classifier = new RT();
			Classifier.buildClassifier(train);
			

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
				double[] confidences = Classifier.distributionForInstance(ins);
				int argmax = -1;
				double max = -1;
					for(int i = 0; i < 6; i++){
						if(confidences[i]>max){
							max = confidences[i];
							argmax = i;
						}
					}
				String sys2ask = systems.get(systems.size() - argmax -1);
				ave_p += Float.parseFloat(Utils.loadSystemP(sys2ask).get(k));				
				ave_r += Float.parseFloat(Utils.loadSystemR(sys2ask).get(k));
			}
			
			double p = ave_p/test.size();
			double r = ave_r/test.size();
			double fmeasure = 0;
			if(p>0&&r>0){fmeasure = 2*p*r/(p + r);}
			System.out.println("macro F on fold " + n + ": " + fmeasure);
			
			cv_ave_f += fmeasure/folds;
						
		}
		System.out.println("macro F average: " + cv_ave_f);
		System.out.println('\n');
	}
}