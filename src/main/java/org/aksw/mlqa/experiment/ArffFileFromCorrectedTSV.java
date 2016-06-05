package org.aksw.mlqa.experiment;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.aksw.mlqa.analyzer.Analyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

@SuppressWarnings("deprecation")
public class ArffFileFromCorrectedTSV {
	static Logger log = LoggerFactory.getLogger(ArffFileFromCorrectedTSV.class);
	
		/*
		 * This class was written to produce an .arff from manually corrected test runs.
		 * The .tsv should have the form : "question	f(HAWK)	f(YODA)	f(QAKIS)"
		 */
	
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {		
		/*
		 * For multilable classification:
		 */
		
		FastVector<String> fvhawk = new FastVector<String>();
		fvhawk.addElement("0");
		fvhawk.addElement("1");
		Attribute hawkatt = new Attribute("hawk", fvhawk);
		
		FastVector<String> fvqakis = new FastVector<String>();
		fvqakis.addElement("0");
		fvqakis.addElement("1");
		Attribute qakisatt = new Attribute("qakis", fvqakis);
		
		FastVector<String> fvyoda = new FastVector<String>();
		fvyoda.addElement("0");
		fvyoda.addElement("1");
		Attribute yodaatt = new Attribute("yoda", fvyoda);
		
		/*
		FastVector fvsina = new FastVector();
		fvsina.addElement("1");
		fvsina.addElement("0");
		Attribute sinaatt = new Attribute("sina", fvsina);
		*/

		/*
		 * 
		 */
		
		// 1. Learn on the training data for each system a classifier to find
		// out which system can answer which question

		// 1.1 load the questions and how good each system answers
		log.debug("Load the questions and how good each system answers");
		Path tsvpath = Paths.get("./src/main/resources/RunDataManuallyCorrected.tsv");
		List<String> tsvlines = Files.lines(tsvpath).collect(Collectors.toList());

		// 1.2 calculate the features per question and system
		log.debug("Calculate the features per question and system");
		Analyzer analyzer = new Analyzer();
		@SuppressWarnings("unchecked")
		ArrayList<Attribute> fvfinal = analyzer.fvWekaAttributes;
		
		fvfinal.add(0, hawkatt);
		fvfinal.add(0, yodaatt);
		//fvfinal.add(0, sinaatt);
		fvfinal.add(0,qakisatt);
		
		
		
		Instances trainingSet = new Instances("training_classifier: -C 3" , fvfinal, tsvlines.size());
		log.debug("Start collection of training data for each system");

	
		for(String x: tsvlines){
			String[] y = x.split("\t");
			String question = y[0];
			Instance tmp = analyzer.analyze(question);

			tmp.setValue(hawkatt, 0);
			tmp.setValue(yodaatt, 0);
			//tmp.setValue(sinaatt, 0);
			tmp.setValue(qakisatt, 0);

			for(int i=1; i<4; i++){
				Double fmeasure = new Double(y[i].replace(",", "."));
				System.out.println(fmeasure);
				if(fmeasure > 0){
					if(i == 1) tmp.setValue(hawkatt, 1);
					if(i == 2) tmp.setValue(yodaatt, 1);
					if(i == 3) tmp.setValue(qakisatt, 1);
				}
			}

			trainingSet.add(tmp);
			System.out.println(tmp.toString());
			}
		log.debug(trainingSet.toString());

		try (FileWriter file = new FileWriter("./src/main/resources/Train.arff")) {
			file.write(trainingSet.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}			
		}
	}
