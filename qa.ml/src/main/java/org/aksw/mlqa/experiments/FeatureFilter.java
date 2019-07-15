package org.aksw.mlqa.experiments;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.mlqa.utils.Utils;

import com.google.common.collect.Lists;

import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

/*
 * This class computes the best label combination. 
 */
public class FeatureFilter {
	
	private ArrayList<String> systems = Lists.newArrayList("AskNow","QAKIS","TebaQA","QASystem", "QANSWER","GANSWER2");
	
	/**
	 * Computes the best feature combination for the given classifier and ARRF file.
	 * @param classifier any multi-label classifier from the MEKA library
	 * @param arffFile only works if this file contains all 14 features in the correct order (order from the Analyzer() constructor)
	 * @throws Exception
	 */
	public void findCombination(Classifier classifier, String arffFile) throws Exception {
		Integer[] in = {6,7,8,9,10,11,12,13,14,15,16,17,18,19};
		
		Set<Integer> ind = new LinkedHashSet<Integer>(Arrays.asList(in));
		Set<LinkedHashSet<Integer>> filters = Utils.powerSet(ind);
				
		Path datapath= Paths.get("./src/main/resources/" + arffFile);
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		
		HashMap<String, ArrayList<Float>> precisions = new HashMap<String, ArrayList<Float>>();
		HashMap<String, ArrayList<Float>> recalls = new HashMap<String, ArrayList<Float>>();
		for(String system: systems){
			precisions.put(system, Utils.loadSystemP(system, data.size()));
		}
		for(String system: systems){
			recalls.put(system, Utils.loadSystemR(system, data.size()));
		}
	
		double curmax = 0;
		Set<Integer> attributes = new HashSet<Integer>();
		int count = 0;
		
		//iterates over all sets in the powerset and removes the features specified in the set
		for(LinkedHashSet<Integer> filter: filters) {
			
			//nothing to check if all features are removed
			if(filter.size() == in.length) {
				continue;
			}
			datapath= Paths.get("./src/main/resources/" + arffFile);
			reader = new BufferedReader(new FileReader(datapath.toString()));
			arff = new ArffReader(reader);
			data = arff.getData();

			List<Integer> filterlist = new ArrayList<Integer>(filter);
			Collections.sort(filterlist);
			
			int k = 0;
			//complete spaghetti code at the moment 
			//removes the features from the instances, lots of special cases because the POS,DEP features consist of multiple attributes
			for(Integer i: filterlist) {
				if(i > 7) {
					if (filterlist.contains(6) && !filterlist.contains(7)) {
						i+=17;
					}else if (filterlist.contains(7) && !filterlist.contains(6)) {
						i+=35;
					} else if (!filterlist.contains(6) && !filterlist.contains(7)) {
						i += 52;
					}
				}
				
				//PartOfSpeechTags feature
				if(i == 6) {
					for(int p=0; p<36; p++) {
						data.deleteAttributeAt(i);
					}
				//Dependencies feature
				} else if (i == 7) {
					int p = 0;
					if(!filterlist.contains(6)) {
						p=35;						
					}                  
					for(int u = 0; u<18; u++) {
						data.deleteAttributeAt(i-k+p);                                                                                                                                                                                                                                                                                                                                                                                                                               
					}
				} else {
					data.deleteAttributeAt(i-k);
				}
				k++;
			}
			data.setClassIndex(6);
			

			classifier.buildClassifier(data);

			float p = 0;
			float r = 0;
			double ave_f = 0;
			for(int j = 0; j < data.size(); j++){
				Instance ins = data.get(j);
				double[] confidences = classifier.distributionForInstance(ins);
				int argmax = -1;
				double max = -1;
				for(int i = 0; i < 6; i++){
					if(confidences[i]>max){
						max = confidences[i];
						argmax = i;
					}	
				}
				String sys2ask = systems.get(systems.size() - argmax -1);
				p = precisions.get(sys2ask).get(j);				
				r = recalls.get(sys2ask).get(j);
				if(p>0&&r>0) {
					ave_f += 2*p*r/(p + r);
				}
			}
			
			double fmeasure = ave_f/data.size();
			
			// if both combinations are equal for 2 decimal places, keep the one with less features
			if((Math.floor(fmeasure* 100) == Math.floor(curmax * 100) && filter.size() > attributes.size()) 
					|| (Math.floor(fmeasure* 100) != Math.floor(curmax * 100) && fmeasure > curmax)){
				curmax = fmeasure;
				attributes = filter;
				System.out.println(filterlist);
				System.out.println(data.toSummaryString());
				System.out.println(fmeasure);
			}
			
			System.out.println(count);
			count++;
		}
		System.out.println("\nMAXIMUM");
		System.out.println(curmax);
		
		Map<Integer, String> featureMap = createFeatureMap();
		for(Integer t: in) {
			if(!attributes.contains(t)) {
				System.out.print(featureMap.get(t) + "  ");
			}
		}
	}
	
	public ArrayList<String> getSystems() {
		return systems;
	}

	public void setSystems(ArrayList<String> systems) {
		this.systems = systems;
	}
	
	private Map<Integer, String> createFeatureMap() {
	    Map<Integer,String> featureMap = new HashMap<Integer,String>();
	    featureMap.put(6, "POS");
	    featureMap.put(7, "DEP");
	    featureMap.put(8, "QT");
	    featureMap.put(9, "QAT");
	    featureMap.put(10, "QW");
	    featureMap.put(11, "#T");
	    featureMap.put(12, "Super");
	    featureMap.put(13, "Comp");
	    featureMap.put(14, "Person");
	    featureMap.put(15, "Money");
	    featureMap.put(16, "Location");
	    featureMap.put(17, "Percent");
	    featureMap.put(18, "Organization");
	    featureMap.put(19, "Date");
	    return featureMap;
	}
	
}