package org.aksw.qa.commons.load.json;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ExtendedQALDJSONLoaderTest {
	
	@Test
	public void extentedJSONTest() throws JsonProcessingException {
		HashMap<String, EJBinding> hash = new HashMap<>();
		hash.put("myVar", new EJBinding().setType("myType").setValue("myValue"));

		ExtendedJson ej = new ExtendedJson();
		EJQuestionEntry entry = new EJQuestionEntry();
		EJAnswers answers = new EJAnswers();
		entry.getQuestion().addAnsweritemtype("answeritemtype").setId(5 + "").setAnswertype("someAnswertype").setConfidence("very confident").setAnswers(answers);
		answers.setHead(new EJHead());
		answers.getHead().getVars().add("myVariable");
		answers.getHead().getLink().add("http://myli.n" + "k");
		EJResults results = new EJResults();
		results.getBindings().add((hash));
		answers.setResults(results);
		answers.setConfidence("so confident, very satisfied, such unafraid").setBoolean(true);
		EJDataset dataset = new EJDataset();
		dataset.setId("5").setMetadata("MetadataString");
		ej.setDataset(dataset);

		ej.addQuestions(entry);
		
		String content = new String(ExtendedQALDJSONLoader.writeJson(ej));
		String realContent = "{\"dataset\":{\"id\":\"5\",\"metadata\":\"MetadataString\"},\"questions\":[{\"question\":{\"id\":\"5\",\"answertype\":\"someAnswertype\",\""
				+ "confidence\":\"very confident\",\"answeritemtype\":[\"answeritemtype\"],\"answers\":{\"head\":{\"vars\":[\"myVariable\"],\"link\":[\"http://myli.nk\"]},\"results\":"
				+ "{\"bindings\":[{\"myVar\":{\"type\":\"myType\",\"value\":\"myValue\"}}]},\"confidence\":\"so confident, very satisfied, such unafraid\",\"boolean\":true}}}]}";
		Assert.assertTrue(realContent.equals(content));
	}
}
