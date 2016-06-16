package org.aksw.qa.commons.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class SPARQLExecutor {

	public static boolean isEndpointAlive(String endpoint) {
		try {
			BufferedReader reader = getReader(endpoint);
			reader.close();
			return true;
		} catch (IOException e) {
		}
		return false;

	}
//TODO change that to use proper JENA library
	public static Results executeSelect(String query, String endpoint) {
		BufferedReader reader;
		try {
			reader = getReader(endpoint + "?query=" + URLEncoder.encode(query, "UTF-8"));
			String results = readAll(reader);
			reader.close();
			Results ret = processSelectResults(results);
			return ret;
		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Boolean executeAsk(String query, String endpoint) {
		BufferedReader reader;
		try {
			reader = getReader(endpoint + "?query=" + URLEncoder.encode(query, "UTF-8"));
			String results = readAll(reader);
			reader.close();
			return Boolean.valueOf(results);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static Results processSelectResults(String results) throws ParserConfigurationException, SAXException, IOException {
		// Set<String> ret = CollectionUtils.newHashSet();
		InputSource is = new InputSource(new StringReader(results));
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList nodes = doc.getFirstChild().getChildNodes();
		Results res = new Results();
		for (int i = 0; i < nodes.getLength(); i++) {
			NodeList childs = nodes.item(i).getChildNodes();

			List<String> row = new LinkedList<String>();
			for (int j = 0; j < childs.getLength(); j++) {
				if (childs.item(j).getNodeName().equals("th")) {
					res.header.add(childs.item(j).getTextContent());
					continue;
				}

				String add = "";
				if (childs.item(j).hasChildNodes()) {
					add = childs.item(j).getFirstChild().getTextContent().trim();
				} else {
					add = childs.item(j).getTextContent().trim();
				}
				if (!add.isEmpty()) {
					row.add(add);
				}
			}
			if (!row.isEmpty()) {
				res.table.add(row);
			}
		}

		return res;
	}

	private static String readAll(BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = reader.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	private static BufferedReader getReader(String endpoint) throws IOException {
		URL url = new URL(endpoint);
		InputStream stream = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
		return reader;
	}

}
