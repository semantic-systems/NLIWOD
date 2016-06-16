package org.aksw.qa.commons.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.aksw.qa.commons.load.Dataset;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

//so the eval tool works http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/index.php?x=evaltool&q=5 
public class StoreQALDXML {

	private Dataset dataset;
	private List<Element> questions;
	private Document doc;

	public StoreQALDXML(Dataset dataset) throws IOException, ParserConfigurationException {
		this.dataset = dataset;
		questions = new ArrayList<Element>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.newDocument();

	}

	public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {

		String dataset = "QALD5_Train";
		StoreQALDXML qw = new StoreQALDXML(Dataset.valueOf(dataset));

		StringBuilder sb = new StringBuilder();
		sb.append("PREFIX text:<http://jena.apache.org/text#> \n");
		sb.append("SELECT DISTINCT ?proj WHERE {\n ");
		sb.append("?const <http://dbpedia.org/ontology/starring> ?proj. ");
		sb.append("?proj text:query (<http://dbpedia.org/ontology/abstract> \"Coquette Productions\"'");
		sb.append("' " + 1000 + "). \n");
		sb.append("}\n");

		Set<String> answerSet = new HashSet<String>();
		answerSet.add("http://dbpedia.org/resource/1");
		answerSet.add("http://dbpedia.org/resource/2");
		String query = sb.toString();
		String question = "Where was the assassin of Martin Luther King born?";
		Integer question_id = 1;
		qw.write(question, query, answerSet, question_id);
		qw.close();
	}

	public void close() throws IOException, TransformerFactoryConfigurationError, TransformerException {
		Element root = doc.createElement("dataset");
		root.setAttribute("id", dataset.toString());
		doc.appendChild(root);
		for (Element question : questions) {
			root.appendChild(question);
		}

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult file = new StreamResult(new File("answer_" + dataset + ".xml"));
		transformer.transform(source, file);

		System.out.println("\nXML DOM Created Successfully..");
	}

	public void write(String questionString, String queryString, Set<String> answerSet, Integer questionID) throws ParserConfigurationException, IOException {

		if (questionString != null) {
			Element question = doc.createElement("question");
			if (questionID != null) {
				question.setAttribute("id", String.valueOf(questionID));
			}
			// TODO adapt to be more flexible. therefore use the question object to write an XML instead of four parameters
			question.setAttribute("answertype", "resource");
			question.setAttribute("aggregation", "false");
			question.setAttribute("onlydbo", "true");
			question.setAttribute("hybrid", "true");

			Element string = doc.createElement("string");
			string.setAttribute("lang", "en");
			string.setTextContent(questionString);
			question.appendChild(string);

			Element pseudoquery = doc.createElement("pseudoquery");
			pseudoquery.setTextContent(queryString);
			question.appendChild(pseudoquery);

			Element answers = doc.createElement("answers");

			for (String uri : answerSet) {
				Element answer = doc.createElement("answer");
				answer.setTextContent(uri);
				answers.appendChild(answer);
			}
			question.appendChild(answers);
			questions.add(question);
		}
	}
}