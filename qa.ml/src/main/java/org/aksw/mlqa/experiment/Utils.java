package org.aksw.mlqa.experiment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

public class Utils {
	
	private static Logger log = LoggerFactory.getLogger(Utils.class);

	public static ArrayList<String> loadSystemP(String system){

		Path datapath = Paths.get("./src/main/resources/QALD6MultilingualLogs/multilingual_" + system + ".html");
		ArrayList<String> result = Lists.newArrayList();

		try{
			String loadedData = Files.lines(datapath).collect(Collectors.joining()); 
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

		try{
			String loadedData = Files.lines(datapath).collect(Collectors.joining()); 
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
	
	public static ArrayList<String> loadSystemData(String system){
		Path datapath = Paths.get("./src/main/resources/QALD6MultilingualLogs/multilingual_" + system + ".html");
		ArrayList<String> result = Lists.newArrayList();

		try{
			String loadedData = Files.lines(datapath).collect(Collectors.joining()); 
			Document doc = Jsoup.parse(loadedData);
			Element table = doc.select("table").get(5);
			Elements tableRows = table.select("tr");
			for(Element row: tableRows){
				Elements tableEntry = row.select("td");
				result.add(tableEntry.get(3).ownText());
			}
			result.remove(0); //remove the head of the table
			return result;
		}catch(IOException e){
			e.printStackTrace();
			log.debug("loading failed.");
			return result;
		}
	}

}
