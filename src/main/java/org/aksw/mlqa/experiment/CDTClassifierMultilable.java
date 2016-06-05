package org.aksw.mlqa.experiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import meka.classifiers.multilabel.CDT;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class CDTClassifierMultilable {
	static Logger log = LoggerFactory.getLogger(CDTClassifierMultilable.class);
	
	
	public static void main(String[] args) throws Exception {		
		/*
		 * For multilable classification:
		 */
		
		//The classifier
		CDT cdt_Classifier = new CDT();
		//load the data
		Path datapath= Paths.get("./src/main/resources/Train.arff");
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(3);
		cdt_Classifier.buildClassifier(data);
		int count = 0;
		for(Instance ins:data){
			double[] confidences = cdt_Classifier.distributionForInstance(ins);
			int argmax = -1;
			double max = -1;
				for(int i = 0; i < 3; i++){
					if(confidences[i]>max){
						max = confidences[i];
						argmax = i;
					}
				}
			System.out.println(argmax);
			System.out.print(ins.stringValue(ins.attribute(0)));
			System.out.print(ins.stringValue(ins.attribute(1)));
			System.out.print(ins.stringValue(ins.attribute(2)));
			System.out.println(" ");
			if(argmax == 1) count+=1;
		}
		System.out.println(count);
			
		
	}
}