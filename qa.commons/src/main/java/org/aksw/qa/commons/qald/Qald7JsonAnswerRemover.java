package org.aksw.qa.commons.qald;

import java.io.File;

import org.aksw.qa.commons.load.json.ExtendedQALDJSONLoader;
import org.aksw.qa.commons.load.json.QaldJson;
import org.aksw.qa.commons.load.json.QaldQuestionEntry;

public class Qald7JsonAnswerRemover {

	
	
	public static void main(String[] args) throws Exception {
		File inputFile=new File("/Users/ricardousbeck/Dropbox (AKSW)/QALD-7/QALD/7/data/qald-7-test-en-wikidata.json");
		File outputFile= new File("/Users/ricardousbeck/Dropbox (AKSW)/QALD-7/QALD/7/data/qald-7-test-en-wikidata-withoutanswers.json");
		

		QaldJson json=(QaldJson)ExtendedQALDJSONLoader.readJson(inputFile, QaldJson.class);
		
		for(QaldQuestionEntry it :json.getQuestions()){
			it.setAnswers(null);
			it.setQuery(null);
		}
		
		ExtendedQALDJSONLoader.writeJson(json, outputFile, true);
		System.out.println("Done");
	}
}
