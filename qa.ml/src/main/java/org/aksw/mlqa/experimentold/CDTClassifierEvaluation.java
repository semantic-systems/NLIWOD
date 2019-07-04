package org.aksw.mlqa.experimentold;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import meka.classifiers.multilabel.PSt;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader.ArffReader;

public class CDTClassifierEvaluation {
	private static Logger log = LoggerFactory.getLogger(CDTClassifierEvaluation.class);
	
	
	public static void main(String[] args) throws Exception {		
		/*
		 * For multilable classification:
		 */
		
		//load the data
		Path datapath= Paths.get("./src/main/resources/old/Qald6Logs.arff");
		BufferedReader reader = new BufferedReader(new FileReader(datapath.toString()));
		ArffReader arff = new ArffReader(reader);
		Instances data = arff.getData();
		data.setClassIndex(6);
		
	    // randomize data
		long seed = System.currentTimeMillis();
		int folds = 100;
		
		String qasystem = "KWGAnswer";
		
		
	    Random rand = new Random(seed);
	    Instances randData = new Instances(data);
	    randData.randomize(rand);
		ArrayList<String> systems = Lists.newArrayList("KWGAnswer", "NbFramework", "PersianQA", "SemGraphQA", "UIQA_withoutManualEntries", "UTQA_English");
		
		
		// perform cross-validation
		Double foldavep = 0.0;
		Double foldaver = 0.0;
		Double foldavef = 0.0;
		Double foldsys = 0.0;

	    for (int n = 0; n < folds; n++) {
	      Instances train = randData.trainCV(folds, n);
	      Instances test = randData.testCV(folds, n);
	      // build and evaluate classifier
	      PSt pst = new PSt();
	      pst.buildClassifier(train);
			float ave_p = 0;
			float ave_r = 0;
			float sysp = 0;
			float sysr = 0;

			for(int j = 0; j < test.size(); j++){
				Instance ins = test.get(j);
				double[] confidences = pst.distributionForInstance(ins);
				int argmax = -1;
				double max = -1;
					for(int i = 0; i < 6; i++){
						if(confidences[i]>max){
							max = confidences[i];
							argmax = i;
						}
					}	
				String sys2ask = systems.get(systems.size() - argmax -1);
				ave_p += Float.parseFloat(loadSystemP(sys2ask).get(j));				
				ave_r += Float.parseFloat(loadSystemR(sys2ask).get(j));
				sysp += Float.parseFloat(loadSystemP(qasystem).get(j));				
				sysr += Float.parseFloat(loadSystemR(sys2ask).get(j));
				}
			double p = ave_p/test.size();
			double r = ave_r/test.size();
			double syspave = sysp/test.size();
			double sysrave = sysr/test.size();
			double sysfmeasure = 2*sysrave*syspave/(sysrave + syspave);
			System.out.println(" RESULT FOR FOLD " + n);
			System.out.println("macro P : " + p);
			System.out.println("macro R : " + r);
			double fmeasure = 2*p*r/(p + r);
			System.out.println("macro F : " + fmeasure + '\n');
			foldavep += p/folds;
			foldaver += r/folds;
			foldavef += fmeasure/folds;
			foldsys += sysfmeasure/folds;
	   }
		System.out.println(" RESULT FOR CV ");
		System.out.println("macro aveP : " + foldavep);
		System.out.println("macro aveR : " + foldaver);
		System.out.println("macro aveF : " + foldavef);
		System.out.println("macro aveF " + qasystem + " : " + foldsys);


	}
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

}

