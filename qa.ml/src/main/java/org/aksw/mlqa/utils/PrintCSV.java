package org.aksw.mlqa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.load.json.EJQuestionFactory;
import org.aksw.qa.commons.load.json.ExtendedQALDJSONLoader;
import org.aksw.qa.commons.load.json.QaldJson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class PrintCSV {
	
	
	/**
	 * Takes an QALD file and calculates precision, recall, F-measures for each question and puts them into a csv file.
	 * ID Precision Recall F-measure
	 */
    public static void main( String[] args ) throws JsonParseException, JsonMappingException, IOException
    {
    	String filePath = "./src/main/resources/QALD9Answers/QANSWER_QALD9_Train.json";
    	String outputPath = "./src/main/resources/QANSWER_QALD9_Train.csv";
    	List<IQuestion> correctAnswers = LoaderController.load(Dataset.QALD9_Train_Multilingual);
    	
    	
    	File file = new File(filePath);
    	InputStream stream = new FileInputStream(file);
    	
		QaldJson json = (QaldJson) ExtendedQALDJSONLoader.readJson(stream, QaldJson.class);
		List<IQuestion> answers = EJQuestionFactory.getQuestionsFromQaldJson(json);
		
		
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
		nf.setMaximumFractionDigits(2);
		
		FileWriter fw = new FileWriter(outputPath);
		System.out.println("ID\tPrecision\tRecall\tF1\n");
		fw.write("ID\tPrecision\tRecall\tF1\n");
		for(int i = 0; i<answers.size(); i++) {
    		Set<String> answer = answers.get(i).getGoldenAnswers();
    		
    		Set<String> correctAnswer = correctAnswers.get(i).getGoldenAnswers();
    		if(answer.size() == 0 || correctAnswer.size() == 0) {
    			System.out.println(correctAnswers.get(i).getId() +"\t0\t0\t0");
    			fw.write(correctAnswers.get(i).getId() +"\t0\t0\t0\n");
    			continue;
    		}
    		int count = 0;
    		for(String real : correctAnswer) {
    			for(String found: answer) {
    				if(real.equals(found)) {
    					count++;
    					break;
    				}
    			}
    		}
		
    		float recall = (float) count/correctAnswer.size();
    		float precision = (float) count/answer.size();
    		if(recall == 0 && precision == 0) {
    			System.out.println(correctAnswers.get(i).getId() +"\t0\t0\t0");
    			fw.write(correctAnswers.get(i).getId() +"\t0\t0\t0\n");
    			continue;
    		}
    		
    		float f1 = 2*((recall*precision)/(recall+precision));
    		//System.out.println(recall + " | " + precision + " | " + f1);
    		System.out.println(correctAnswers.get(i).getId() +"\t" + precision + "\t" + recall + "\t" + f1);
    		fw.write(correctAnswers.get(i).getId() +"\t" + precision + "\t" + recall + "\t" + f1 + "\n");
		}
		fw.close();
    }
}
