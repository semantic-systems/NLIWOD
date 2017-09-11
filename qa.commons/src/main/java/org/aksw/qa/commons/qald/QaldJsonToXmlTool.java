package org.aksw.qa.commons.qald;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.json.EJQuestionFactory;
import org.aksw.qa.commons.load.json.ExtendedQALDJSONLoader;
import org.aksw.qa.commons.load.json.QaldJson;
import org.aksw.qa.commons.store.StoreQALDXML;

public class QaldJsonToXmlTool {
	public static void main(final String[] args) throws ParserConfigurationException, IOException, TransformerFactoryConfigurationError, TransformerException {

		File inputFile = new File("qald-7-test-multilingual.json");
		String outputPathWithDatasetNameAndFileExtension = "qald-7-test-multilingual.xml";
		//The name that will be set in the xml for this dataset
		String datasetName = "qald-7-test-multilingual";

		QaldJson json = (QaldJson) ExtendedQALDJSONLoader.readJson(inputFile, QaldJson.class);

		//Uncomment for creating extended json also

		//ExtendedJson ejson = EJQuestionFactory.fromQaldToExtended(json);
		//ExtendedQALDJSONLoader.writeJson(ejson, new File("qald-7-train-hybrid-extended-json.json"), true);

		StoreQALDXML xml = new StoreQALDXML(datasetName);

		List<IQuestion> goodQuestions = EJQuestionFactory.getQuestionsFromQaldJson(json);
		for (IQuestion q : goodQuestions) {
			xml.write(q);
		}
		xml.close(outputPathWithDatasetNameAndFileExtension, datasetName);
		System.out.println("Done");
	}
}
