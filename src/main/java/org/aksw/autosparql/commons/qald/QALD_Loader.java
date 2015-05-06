package org.aksw.autosparql.commons.qald;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 */
public class QALD_Loader {

	public static void main(String[] args) {

		String file = new File("resources/qald-5_train.xml").getAbsolutePath();
		QALD_Loader ql = new QALD_Loader();
		for (Question q : ql.load(file)) {
			System.out.println(q.languageToQuestion);
			System.out.println("\tAnswers: " + StringUtils.join(q.goldenAnswers, ", "));
		}
	}

	public List<Question> load(String file) {

		List<Question> questions = new ArrayList<Question>();

		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(file);
			doc.getDocumentElement().normalize();

			NodeList questionNodes = doc.getElementsByTagName("question");

			for (int i = 0; i < questionNodes.getLength(); i++) {

				Question question = new Question();
				Element questionNode = (Element) questionNodes.item(i);

				question.id = Integer.valueOf(questionNode.getAttribute("id"));
				question.answerType = questionNode.getAttribute("answertype");
				question.aggregation = Boolean.valueOf(questionNode.getAttribute("aggregation"));
				question.onlydbo = Boolean.valueOf(questionNode.getAttribute("onlydbo"));
				question.hybrid = Boolean.valueOf(questionNode.getAttribute("hybrid"));

				// Read question
				NodeList nlrs = questionNode.getElementsByTagName("string");
				for (int j = 0; j < nlrs.getLength(); j++) {
					String lang = ((Element) nlrs.item(j)).getAttribute("lang");
					question.languageToQuestion.put(lang, ((Element) nlrs.item(j)).getTextContent().trim());
				}

				// read keywords
				NodeList keywords = questionNode.getElementsByTagName("keywords");
				for (int j = 0; j < keywords.getLength(); j++) {
					String lang = ((Element) keywords.item(j)).getAttribute("lang");
					question.languageToKeywords.put(lang, Arrays.asList(((Element) keywords.item(j)).getTextContent().trim().split(", ")));
				}

				// Read pseudoSPARQL query
				Element element = (Element) questionNode.getElementsByTagName("pseudoquery").item(0);
				if (element != null) {
					NodeList childNodes = element.getChildNodes();
					Node item = childNodes.item(0);
					question.pseudoSparqlQuery = item.getNodeValue().trim();
				}

				// Read SPARQL query
				element = (Element) questionNode.getElementsByTagName("query").item(0);
				if (element != null) {
					NodeList childNodes = element.getChildNodes();
					Node item = childNodes.item(0);
					question.sparqlQuery = item.getNodeValue().trim();
				}
				// check if OUT OF SCOPE marked
				if (question.pseudoSparqlQuery != null) {
					question.outOfScope = question.pseudoSparqlQuery.toUpperCase().contains("OUT OF SCOPE");
				}
				// read answers
				NodeList answers = questionNode.getElementsByTagName("answer");
				HashSet<String> set = new HashSet<>();
				for (int j = 0; j < answers.getLength(); j++) {
					String answer = ((Element) answers.item(j)).getTextContent();
					set.add(answer.trim());
				}
				question.goldenAnswers.put("en", set);
				questions.add(question);
			}

		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return questions;
	}

}
