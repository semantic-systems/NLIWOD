package org.aksw.mlqa.analyzer.nqs;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class XMLReader {

  public static void main(String argv[]) {

    try {

	File fXmlFile = new File("staff.xml");
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);
			
	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();

	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			
	NodeList nList = doc.getElementsByTagName("staff");
			
	System.out.println("----------------------------");

	for (int temp = 0; temp < nList.getLength(); temp++) {

		Node nNode = nList.item(temp);
				
		System.out.println("\nCurrent Element :" + nNode.getNodeName());
				
		if (nNode.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) nNode;

			System.out.println("Staff id : " + eElement.getAttribute("id"));
			System.out.println("First Name : " + eElement.getElementsByTagName("firstname").item(0).getTextContent());
			System.out.println("First Name ID: " + eElement.getElementsByTagName("firstname").item(0).getAttributes().getNamedItem("id"));
			System.out.println("Last Name : " + eElement.getElementsByTagName("lastname").item(0).getTextContent());
			System.out.println("Nick Name : " + eElement.getElementsByTagName("nickname").item(0).getTextContent());
			System.out.println("Salary : " + eElement.getElementsByTagName("salary").item(0).getTextContent());

		}
	}
    } catch (Exception e) {
	e.printStackTrace();
    }
    
    try {

	File fXmlFile = new File("q5.xml");
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);
			
	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();

	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
			
	NodeList nList = doc.getElementsByTagName("question");
			
	System.out.println("----------------------------");
	List<String> keywords = null;

	for (int temp = 0; temp < nList.getLength(); temp++) {

		Node nNode = nList.item(temp);
		System.out.println("\nCurrent Element :" + nNode.getNodeName() +" id:"+nNode.getAttributes().getNamedItem("id").getTextContent());
		for(int i=0;i<nNode.getChildNodes().getLength();i++){
			if(nNode.getChildNodes().item(i).getNodeName().equals("string") 
					&& nNode.getChildNodes().item(i).getAttributes().getNamedItem("lang").getNodeValue().equals("en")){
				System.out.println("Question: "+nNode.getChildNodes().item(i).getTextContent());
			}
			
			if(nNode.getChildNodes().item(i).getNodeName().equals("keywords") 
					&& nNode.getChildNodes().item(i).getAttributes().getNamedItem("lang").getNodeValue().equals("en")){
				System.out.println("Keywords: "+nNode.getChildNodes().item(i).getTextContent());
				keywords = Arrays.asList(nNode.getChildNodes().item(i).getTextContent().split(" "));
			}
		}
		System.out.println(keywords.toString());
	}
    } catch (Exception e) {
	e.printStackTrace();
    }
  }

}