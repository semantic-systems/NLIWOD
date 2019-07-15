package org.aksw.mlqa.utilold;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.aksw.mlqa.experimentold.RunProducer;
import org.aksw.qa.commons.load.Dataset;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONtoCSV {
	
	public static void main (String[] args){
		JSONArray rundata = RunProducer.loadRunData(Dataset.QALD6_Train_Multilingual);
		Path outpath = Paths.get("./src/main/resources/RunData.CSV");

		try {
			FileWriter writer = new FileWriter(outpath.toString());
			writer.append("Question id" + "\t");
			writer.append("question" + "\t");
			writer.append("goldAnswers" + "\t");
			writer.append("HawkAnswers" + "\t");
			writer.append("HawkFmeasure" + "\t");
			writer.append("YodaAnswers" + "\t");
			writer.append("YodaFmeasure" + "\t");
			writer.append("QakisAnswers" + "\t");
			writer.append("QakisFmeasure" + "\n");
			for(int i = 0; i < rundata.size(); i++){
				JSONObject questiondata = (JSONObject) rundata.get(i);
				JSONObject answersdata = (JSONObject) questiondata.get("answers");
				JSONObject hawkdata = (JSONObject) answersdata.get("hawk");
				JSONObject yodadata = (JSONObject) answersdata.get("yoda");
				JSONObject qakisdata = (JSONObject) answersdata.get("qakis");
				Long id = (Long) questiondata.get("id");
				List<JSONObject> systemsdata = Arrays.asList(hawkdata,yodadata,qakisdata);
				writer.append(id + "\t");
				String question = (String) questiondata.get("question");
				writer.append(question + "\t");
				String goldanswers = questiondata.get("goldanswers").toString();
				writer.append( goldanswers + "\t");
				for(JSONObject systemdata: systemsdata){
					String systemanswers = systemdata.get("foundAnswers").toString();
					Object systemfmeasure = systemdata.get("fmeasure");
					writer.append(systemanswers + "\t");
					writer.append(systemfmeasure + "\t");
				}
				writer.append("\n");
			}
			writer.close();
	} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
