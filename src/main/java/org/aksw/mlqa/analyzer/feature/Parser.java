package org.aksw.mlqa.analyzer.feature;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
//import org.aksw.asknow.util.XmlUtil;
import org.w3c.dom.*;
import lombok.SneakyThrows;

/** Reads a NQS QALD benchmark template XML file and returns a set of QCT templates. */
public class Parser
{
	/**
	 * Parses the default benchmark (QALD-5 NQS)
	 * @return The list of NQS from QALD-5.*/
	public static List<Nqs> parse()
	{
		return parse(()->Parser.class.getClassLoader().getResourceAsStream("qald-nqs.xml"));
	
	}

	/** @param in supplies a NQS-modified QALD XML format benchmark. Needs to supply a fresh stream each time.
	 * @return a list of NQS as written in the benchmark in the same order. */
	@SneakyThrows
	// TODO: detailed exception handling later
	public static List<Nqs> parse(Supplier<InputStream> in)
	{
		List<Nqs> templates = new ArrayList<>();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in.get());
		doc.getDocumentElement().normalize();
		//if(!XmlUtil.validateAgainstXSD(in.get(), Parser.class.getClassLoader().getResourceAsStream("nqs.xsd")))
		//{throw new IllegalArgumentException("QCT template file not valid against the XSD.");}

		NodeList nList = doc.getElementsByTagName("QALDquestions");
		for (int i = 0; i < nList.getLength(); i++)
		{
			Node nNode = nList.item(i);

			if (nNode.getNodeType() == Node.ELEMENT_NODE)
			{
				Element eElement = (Element) nNode;

				String queryId = eElement.getAttribute("id");
				String nlQuery = eElement.getElementsByTagName("Ques").item(0).getTextContent();
				String qct = eElement.getElementsByTagName("NQS").item(0).getTextContent();
				String ner = eElement.getElementsByTagName("NER").item(0).getTextContent();
				templates.add(new Nqs(nlQuery,qct,queryId,ner));
			}
		}
		return templates;
	}

}