package org.aksw.mlqa.analyzer.nqs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author Crunchify.com
 */

public class XMLWriter {

	private static Element mainRootElement;
	private static Document doc;

	public static void main(String[] args) {
		XMLWriter writer = new XMLWriter("Qald5-Test","Queries");
		writer.appendQuery("1", "Paypal", Arrays.asList("Payment","Payment2"),"1000");
		writer.appendQuery("2", "Paypal2", Arrays.asList("Payment","Payment2"),"2000");
		writer.appendQuery("3", "Paypal3", Arrays.asList("Payment","Payment2"),"3000");
		writer.pubishXML("OutputXML.xml");
	}

	public XMLWriter(String inputXMLFilePath, String DOMRootName) {
		DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder icBuilder;
		try {
			icBuilder = icFactory.newDocumentBuilder();
			doc = icBuilder.newDocument();
			mainRootElement = doc
					.createElementNS(inputXMLFilePath, DOMRootName);
			doc.appendChild(mainRootElement);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public static void appendQuery(String id, String query, List<String> keywords,
			String qct) {
		mainRootElement.appendChild(getQuery(doc, id, query, keywords, qct));
	}

	public static void pubishXML(String outputFileName) {
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			DOMSource source = new DOMSource(doc);
			StreamResult output = new StreamResult(outputFileName);
			transformer.transform(source, output);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private static Node getQuery(Document doc, String id, String query,
			List<String> keywords, String qct) {
		Element company = doc.createElement("Query");
		company.setAttribute("id", id);
		company.appendChild(getQueryElements(doc, "NLquery", query));
		if(keywords!=null){
			String keywordsNormalizedString = keywords.toString().replace("[", "");
			keywordsNormalizedString = keywordsNormalizedString.replace("]", "");
			company.appendChild(getQueryElements(doc,"Keywords", keywordsNormalizedString));
			
			// If multiple keywords needed:
			/*for(String key: keywords){
				company.appendChild(getQueryElements(doc,"Keyword", key));
			}	*/	
		} else{
			System.err.println("Keywords null");
		}
		company.appendChild(getQueryElements(doc, "QCT", qct));
		return company;
	}

	// utility method to create text node
	private static Node getQueryElements(Document doc,
			String name, String value) {
		Element node = doc.createElement(name);
		node.appendChild(doc.createTextNode(value));
		return node;
	}
}