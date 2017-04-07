package org.aksw.qa.commons.qald;

import java.io.File;

import org.aksw.qa.commons.load.json.ExtendedQALDJSONLoader;
import org.aksw.qa.commons.load.json.QaldJson;
import org.aksw.qa.commons.load.json.QaldQuestionEntry;

public class Qald7JsonAnswerRemover {

	
	
	public static void main(String[] args) throws Exception {
		File inputFile=new File("c:/output/data/qald-7-test-multilingual.json");
		File outputFile= new File("c:/output/data/qald-7-test-multilingual-withoutanswers.json");
		

		QaldJson json=(QaldJson)ExtendedQALDJSONLoader.readJson(inputFile, QaldJson.class);
		
		for(QaldQuestionEntry it :json.getQuestions()){
			it.setAnswers(null);
		}
		
		ExtendedQALDJSONLoader.writeJson(json, outputFile, true);
		System.out.println("Done");
	}
}
