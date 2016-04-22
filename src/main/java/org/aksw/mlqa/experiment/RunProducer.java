package org.aksw.mlqa.experiment;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.aksw.mlqa.systems.ASystem;
import org.aksw.mlqa.systems.HAWK;
import org.aksw.mlqa.systems.QAKIS;
import org.aksw.mlqa.systems.SINA;
import org.aksw.mlqa.systems.YODA;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RunProducer {
	static Logger log = LoggerFactory.getLogger(RunProducer.class);

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		JSONArray rundata = new JSONArray();
		HAWK hawk = new HAWK();
		SINA sina = new SINA();
		QAKIS qakis = new QAKIS();
		YODA yoda = new YODA();
		ArrayList<ASystem> systems = Lists.newArrayList(hawk,sina,qakis,yoda);
		List<IQuestion> questions = QALD_Loader.load(Dataset.QALD6_Train_Multilingual);
		
		for(IQuestion question: questions){			
			JSONObject questiondata = new JSONObject();
			questiondata.put("answertype", question.getAnswerType());
			questiondata.put("goldanswers", question.getGoldenAnswers());
			for(ASystem system: systems){
				questiondata.put(system.name(), system.search(question.getLanguageToQuestion().get("en")));
			}
			rundata.add(questiondata);
			log.debug("Just wrote: " + questiondata.toJSONString());		
		}
		
		try (FileWriter file = new FileWriter("./src/main/resources/QALD6_Train_Multilingual_Answers.txt")) {
			file.write(rundata.toJSONString());
		}
	}

}
