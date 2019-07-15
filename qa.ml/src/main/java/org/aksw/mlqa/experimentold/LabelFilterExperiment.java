package org.aksw.mlqa.experimentold;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import meka.classifiers.multilabel.FW;
import meka.classifiers.multilabel.PSt;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.unsupervised.attribute.Remove;

/*
 * Purpose of this Class is to test the influence of different label combinations on the training score 
 */

public class LabelFilterExperiment {
	// private static Logger log = LoggerFactory.getLogger(LabelFilterExperiment.class);
	
	
	public static void main(String[] args) throws Exception {

		Set<Integer> ind = new HashSet<Integer>(Arrays.asList(7));
		Set<LinkedHashSet<Integer>> filters = Utils.powerSet(ind);
		System.out.println(filters);
//		Path datapath= Paths.get("./src/main/resources/Qald6Logs.arff");
//		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
//		ArffReader arff = new ArffReader(reader);
//		Instances data = arff.getData();
//		data.setClassIndex(6);	
				
		ArrayList<String> systems = Lists.newArrayList("KWGAnswer", "NbFramework", "PersianQA", "SemGraphQA", "UIQA_withoutManualEntries", "UTQA_English" );
		HashMap<String, ArrayList<String>> precisions = new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> recalls = new HashMap<String, ArrayList<String>>();
		for(String system: systems){
			precisions.put(system, Utils.loadSystemP(system));
		}
		for(String system: systems){
			recalls.put(system, Utils.loadSystemR(system));
		}

		
		double curmax = 0;
		String attributes = "";

		for(Set<Integer> filter:filters){
			Path datapath= Paths.get("./src/main/resources/old/Qald6Logs.arff");
			BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
			ArffReader arff = new ArffReader(reader);
			Instances data = arff.getData();
			data.setClassIndex(6);	
			List<Integer> filterlist = new ArrayList<Integer>(filter);
			System.out.println(filterlist);
			int[] atts = new int[filter.size()];
			Remove rm = new Remove();
			for(int i =0; i < filterlist.size(); i++) atts[i] = filterlist.get(i);
			rm.setAttributeIndicesArray(atts);
			
			//Put Classifier of Choice Here
			FW Classifier = new FW();
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(rm);
			fc.setClassifier(Classifier);
			data.setClassIndex(6);	
			fc.buildClassifier(data);

			
			float p = 0;
			float r = 0;
			double ave_f = 0;
			for(int j = 0; j < data.size(); j++){
				Instance ins = data.get(j);
				double[] confidences = fc.distributionForInstance(ins);
				int argmax = -1;
				double max = -1;
				for(int i = 0; i < 6; i++){
					if(confidences[i]>max){
						max = confidences[i];
						argmax = i;
						}	
					}
				String sys2ask = systems.get(systems.size() - argmax -1);
				p = Float.parseFloat(precisions.get(sys2ask).get(j));				
				r = Float.parseFloat(recalls.get(sys2ask).get(j));
				if(p>0&&r>0){
				ave_f += 2*p*r/(p + r);
					}
				}
			
			double fmeasure = ave_f/data.size();
			if(fmeasure > curmax){curmax = fmeasure; attributes = rm.getAttributeIndices();}
			System.out.println(fmeasure);
			System.out.println(rm.getAttributeIndices());
			System.out.println("Indices:" + rm.getAttributeIndices());
			System.out.println("---------------");
			}
		System.out.println("\n MAXIMUM");
		System.out.println(curmax);
		System.out.println(attributes);
	}
	
	
}