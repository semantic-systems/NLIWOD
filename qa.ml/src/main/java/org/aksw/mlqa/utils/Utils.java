package org.aksw.mlqa.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class Utils {
	
	/**
	 * Loads the precision values for the given system for each question.
	 * @param system name of a system
	 * @return list of the precision values for each question
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static ArrayList<Float> loadSystemP(String system, int size) throws JsonParseException, JsonMappingException, IOException {
		String qald = "";
		//we only have data for QALD8 and QALD9
		if(size == 219) {
			qald = "QALD8";
		} else {
			qald = "QALD9";
		}
		File file = new File("./src/main/resources/" + qald + "Answers/" + system + "_" + qald +"_Train.csv");
		BufferedReader br = new BufferedReader(new FileReader(file)) ;
		String line;
		
		ArrayList<Float> recall = new ArrayList<Float>();
		line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] values = line.split("\t");
			recall.add(Float.parseFloat(values[1]));
		}
		br.close();
		return recall;
	}
	
	/**
	 * Loads the recall values for the given system for each question.
	 * @param system name of a system
	 * @param size size of the current dataset
	 * @return list of the recall values for each question
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static ArrayList<Float> loadSystemR(String system, int size) throws JsonParseException, JsonMappingException, IOException {
		String qald = "";
		//we only have data for QALD8 and QALD9
		if(size == 219) {
			qald = "QALD8";
		} else {
			qald = "QALD9";
		}
		File file = new File("./src/main/resources/" + qald + "Answers/" + system + "_" + qald +"_Train.csv");
		BufferedReader br = new BufferedReader(new FileReader(file)) ;
		String line;
		
		ArrayList<Float> recall = new ArrayList<Float>();
		line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] values = line.split("\t");
			recall.add(Float.parseFloat(values[2]));
		}
		br.close();
		return recall;
	}
		
	/**
	 * Computes the powerset of the given set.
	 * @param originalSet
	 * @return powerset
	 */
	public static <T> Set<LinkedHashSet<T>> powerSet(Set<T> originalSet) {
	    Set<LinkedHashSet<T>> sets = new HashSet<LinkedHashSet<T>>();
	    if (originalSet.isEmpty()) {
	    	sets.add(new LinkedHashSet<T>());
	    	return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
	    for (LinkedHashSet<T> set : powerSet(rest)) {
	    	LinkedHashSet<T> newSet = new LinkedHashSet<T>();
	    	newSet.add(head);
	    	newSet.addAll(set);
	    	sets.add(newSet);
	    	sets.add(set);
	    }		
	    return sets;
	}
	
	/**
	 * Loads the F-measure for the given system for each questions.
	 * @param system name of a system
	 * @return list of F-measures for each question
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static ArrayList<Float> loadSystemF(String system, int size) throws JsonParseException, JsonMappingException, IOException {
		String qald = "";
		//we only have data for QALD8 and QALD9
		if(size == 219) {
			qald = "QALD8";
		} else {
			qald = "QALD9";
		}
		File file = new File("./src/main/resources/" + qald + "Answers/" + system + "_" + qald +"_Train.csv");
		BufferedReader br = new BufferedReader(new FileReader(file)) ;
		String line;
		
		ArrayList<Float> f1 = new ArrayList<Float>();
		line = br.readLine();
		while ((line = br.readLine()) != null) {
			String[] values = line.split("\t");
			f1.add(Float.parseFloat(values[3]));
		}
		br.close();
		return f1;
	}
}
