package org.aksw.qa.commons.store;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Joiner;

// so the eval tool works
// http://greententacle.techfak.uni-bielefeld.de/~cunger/qald/index.php?x=evaltool&q=5
public class StoreQALDXML {

	private final String dataset;
	private final List<Element> questions;
	private final Document doc;

	public StoreQALDXML(final String dataset) throws IOException, ParserConfigurationException {
		this.dataset = dataset;
		questions = new ArrayList<>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		doc = db.newDocument();

	}

	// public static void main(final String[] args) throws IOException,
	// ParserConfigurationException, TransformerFactoryConfigurationError,
	// TransformerException {
	//
	// String dataset = "QALD5_Train";
	// StoreQALDXML qw = new StoreQALDXML(dataset);
	//
	// StringBuilder sb = new StringBuilder();
	// sb.append("PREFIX text:<http://jena.apache.org/text#> \n");
	// sb.append("SELECT DISTINCT ?proj WHERE {\n ");
	// sb.append("?const <http://dbpedia.org/ontology/starring> ?proj. ");
	// sb.append("?proj text:query (<http://dbpedia.org/ontology/abstract>
	// \"Coquette Productions\"'");
	// sb.append("' " + 1000 + "). \n");
	// sb.append("}\n");
	//
	// Set<String> answerSet = new HashSet<>();
	// answerSet.add("http://dbpedia.org/resource/1");
	// answerSet.add("http://dbpedia.org/resource/2");
	// String query = sb.toString();
	// String question = "Where was the assassin of Martin Luther King born?";
	// Integer question_id = 1;
	// qw.write(question, query, answerSet, question_id);
	// qw.close();
	// }

	public static void main(final String[] args) {

	}

	public void close() throws IOException, TransformerFactoryConfigurationError, TransformerException {
		Element root = doc.createElement("dataset");
		root.setAttribute("id", dataset);
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

	public void close(final String path, final String datasetName) throws IOException, TransformerFactoryConfigurationError, TransformerException {
		Element root = doc.createElement("dataset");
		root.setAttribute("id", datasetName);
		doc.appendChild(root);
		for (Element question : questions) {
			root.appendChild(question);
		}

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult file = new StreamResult(new File(path));
		transformer.transform(source, file);

		System.out.println("\nXML DOM Created Successfully..");
	}

	public void write(final String questionString, final String queryString, final Set<String> answerSet, final Integer questionID) throws ParserConfigurationException, IOException {

		if (questionString != null) {
			Element question = doc.createElement("question");
			if (questionID != null) {
				question.setAttribute("id", String.valueOf(questionID));
			}
			// TODO adapt to be more flexible. therefore use the question object
			// to write an XML instead of four parameters
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

	public void write(final IQuestion q) throws ParserConfigurationException, IOException {

		Element question = doc.createElement("question");

		question.setAttribute("id", String.valueOf(q.getId()));

		// TODO adapt to be more flexible. therefore use the question object to
		// write an XML instead of four parameters
		question.setAttribute("answertype", q.getAnswerType());
		question.setAttribute("aggregation", "" + q.getAggregation());
		question.setAttribute("onlydbo", "" + q.getOnlydbo());
		question.setAttribute("hybrid", "" + q.getHybrid());

		for (String key : q.getLanguageToQuestion().keySet()) {
			if (Strings.isNullOrEmpty(q.getLanguageToQuestion().get(key))) {
				continue;
			}

			Element string = doc.createElement("string");
			string.setAttribute("lang", key);
			string.setTextContent(q.getLanguageToQuestion().get(key));
			question.appendChild(string);
		}
		for (String key : q.getLanguageToKeywords().keySet()) {
			if (org.apache.commons.collections.CollectionUtils.isEmpty(q.getLanguageToKeywords().get(key))) {
				continue;
			}
			Element keyword = doc.createElement("keywords");
			keyword.setAttribute("lang", key);
			keyword.setTextContent(Joiner.on(", ").join(q.getLanguageToKeywords().get(key)));
			question.appendChild(keyword);
		}

		if (!((q.getPseudoSparqlQuery() == null) || q.getPseudoSparqlQuery().isEmpty())) {
			Element pseudoquery = doc.createElement("pseudoquery");
			pseudoquery.setTextContent(q.getPseudoSparqlQuery());
			question.appendChild(pseudoquery);
		}
		if (!((q.getSparqlQuery() == null) || q.getSparqlQuery().isEmpty())) {
			Element query = doc.createElement("query");
			query.setTextContent(q.getSparqlQuery());
			question.appendChild(query);
		}

		Element answers = doc.createElement("answers");

		for (String uri : q.getGoldenAnswers()) {
			Element answer = doc.createElement("answer");
			answer.setTextContent(uri);
			answers.appendChild(answer);
		}
		question.appendChild(answers);
		questions.add(question);

	}
}