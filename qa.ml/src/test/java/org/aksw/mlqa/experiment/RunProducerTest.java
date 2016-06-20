package org.aksw.mlqa.experiment;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Test;

public class RunProducerTest {

	@Test
	public void test() {
		JSONArray arr = RunProducer.loadRunData(Dataset.QALD6_Train_Multilingual);
		List<IQuestion> trainQuestions = QALD_Loader.load(Dataset.QALD6_Train_Multilingual);

		JSONObject obj = (JSONObject) arr.get(1);
		JSONObject allsystemdata = (JSONObject) obj.get("answers");
		JSONObject systemdata = (JSONObject) allsystemdata.get("hawk");
		System.out.println(obj.toJSONString());
		System.out.println(new Double(systemdata.get("fmeasure").toString()));
		String question = obj.get("question").toString();
		System.out.println(allsystemdata.get("hawk"));
		assertTrue(trainQuestions.get(1).getLanguageToQuestion().get("en").equals(question));
		
	}

}
