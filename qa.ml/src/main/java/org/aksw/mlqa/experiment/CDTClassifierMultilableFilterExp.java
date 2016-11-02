package org.aksw.mlqa.experiment;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import meka.classifiers.multilabel.PSt;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.unsupervised.attribute.Remove;

public class CDTClassifierMultilableFilterExp {
	static Logger log = LoggerFactory.getLogger(CDTClassifierMultilableFilterExp.class);
	
	
	public static void main(String[] args) throws Exception {
		/*
		 * For multilable classification:
		 */
		Set<Integer> ind = new HashSet<Integer>(Arrays.asList(7,8,9,10,11,12,13,14,15,16,17,18));
		Set<Set<Integer>> filters = powerSet(ind);
		
		Path datapath= Paths.get("./src/main/resources/Qald6Logs.arff");
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(6);	
		
		JSONObject qald6test = loadTestQuestions();
		JSONArray questions = (JSONArray) qald6test.get("questions");
		ArrayList<String> testQuestions = Lists.newArrayList();
		for(int i = 0; i < questions.size(); i++){
			JSONObject questionData = (JSONObject) questions.get(i);
			JSONArray questionStrings = (JSONArray) questionData.get("question");
			JSONObject questionEnglish = (JSONObject) questionStrings.get(0);
			testQuestions.add((String) questionEnglish.get("string"));
			}		
		for(Set<Integer> filter:filters){
			List<Integer> filterlist = new ArrayList<Integer>(filter);
			int[] atts = new int[filter.size()];
			Remove rm = new Remove();
			for(int i =0; i < filterlist.size(); i++) atts[i] = filterlist.get(i);
			rm.setAttributeIndicesArray(atts);
			PSt PSt_Classifier = new PSt();
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(rm);
			fc.setClassifier(PSt_Classifier);
			fc.buildClassifier(data);

	
			
			ArrayList<String> systems = Lists.newArrayList("KWGAnswer", "NbFramework", "PersianQA", "SemGraphQA", "UIQA_withoutManualEntries", "UTQA_English" );
			float p = 0;
			float r = 0;
			double ave_f = 0;
			/*
			Double ave_bestp = 0.0;
			Double ave_bestr = 0.0;
			*/
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
				p = Float.parseFloat(loadSystemP(sys2ask).get(j));				
				r = Float.parseFloat(loadSystemR(sys2ask).get(j));
				if(p>0&&r>0){
				ave_f += 2*p*r/(p + r);
					}
				}
			
			double fmeasure = ave_f/data.size();
			System.out.println(fmeasure);
			System.out.println(rm.getAttributeIndices());
			}
		/*
		 * calculate best possible fmeasure
		 

		double bestp = ave_bestp/data.size();
		double bestr = ave_bestr/data.size();
		System.out.println("best possible macro P : " + bestp);
		System.out.println("best possible macro R : " + bestr);
		double bestfmeasure = 2*bestp*bestr/(bestp + bestr);
		System.out.println("best possible macro F : " + bestfmeasure);
		 */
	}
	
	public static ArrayList<String> loadSystemP(String system){

		Path datapath = Paths.get("./src/main/resources/QALD6MultilingualLogs/multilingual_" + system + ".html");
		ArrayList<String> result = Lists.newArrayList();
		String loadedData = "";

		try{
			FileInputStream fstream = new FileInputStream(datapath.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
			  loadedData+=strLine;
			}
			br.close();
			Document doc = Jsoup.parse(loadedData);
			Element table = doc.select("table").get(5);
			Elements tableRows = table.select("tr");
			for(Element row: tableRows){
				Elements tableEntry = row.select("td");
				result.add(tableEntry.get(2).ownText());
			}
			result.remove(0); //remove the head of the table
			return result;
		}catch(IOException e){
			e.printStackTrace();
			log.debug("loading failed.");
			return result;
		}
	}
	public static ArrayList<String> loadSystemR(String system){
		Path datapath = Paths.get("./src/main/resources/QALD6MultilingualLogs/multilingual_" + system + ".html");
		ArrayList<String> result = Lists.newArrayList();
		String loadedData = "";

		try{
			FileInputStream fstream = new FileInputStream(datapath.toString());
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
			  loadedData+=strLine;
			}
			br.close();
			Document doc = Jsoup.parse(loadedData);
			Element table = doc.select("table").get(5);
			Elements tableRows = table.select("tr");
			for(Element row: tableRows){
				Elements tableEntry = row.select("td");
				result.add(tableEntry.get(1).ownText());
			}
			result.remove(0); //remove the head of the table
			return result;
		}catch(IOException e){
			e.printStackTrace();
			log.debug("loading failed.");
			return result;
		}
	}
	
	public static JSONObject loadTestQuestions(){
		String loadeddata;
		try {			
			Path datapath = Paths.get("./src/main/resources/qald-6-test-multilingual.json");
			loadeddata = Files.lines(datapath).collect(Collectors.joining());
			JSONParser parser = new JSONParser();
			JSONObject arr = (JSONObject) parser.parse(loadeddata);
			return arr;
		} catch (IOException | ParseException  e) {
			e.printStackTrace();
			log.debug("loading failed.");
			return new JSONObject();
		}
	}
	
	public static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
	    Set<Set<T>> sets = new HashSet<Set<T>>();
	    if (originalSet.isEmpty()) {
	    	sets.add(new HashSet<T>());
	    	return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    Set<T> rest = new HashSet<T>(list.subList(1, list.size())); 
	    for (Set<T> set : powerSet(rest)) {
	    	Set<T> newSet = new HashSet<T>();
	    	newSet.add(head);
	    	newSet.addAll(set);
	    	sets.add(newSet);
	    	sets.add(set);
	    }		
	    return sets;
	}

}