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
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aksw.qa.commons.qald.QALD4_EvaluationUtils;
import org.aksw.qa.commons.sparql.SPARQL;
import org.aksw.qa.commons.sparql.ThreadedSPARQL;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;

//@Deprecated
/**
 * In qa.commons, there are 2 differnet ways to fire queries to an endpoint, {@link SPARQL} and this class. This becomes tedious to keep clean, especially when both share the same (hardcopied) code.
 * Please consider merging the functionality in favor of {@link SPARQL} or {@link ThreadedSPARQL}
 *
 * @param service
 * @param query
 * @return
 */
public class SPARQLExecutor {

	public static boolean isEndpointAlive(final String endpoint) {
		try {
			BufferedReader reader = getReader(endpoint);
			reader.close();
			return true;
		} catch (IOException e) {
		}
		return false;

	}

	//TODO change that to use proper JENA library
	public static Results executeSelect(final String query, final String endpoint) {
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

	public static Boolean executeAsk(final String query, final String endpoint) {
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

	private static Results processSelectResults(final String results) throws ParserConfigurationException, SAXException, IOException {
		// Set<String> ret = CollectionUtils.newHashSet();
		InputSource is = new InputSource(new StringReader(results));
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(is);
		NodeList nodes = doc.getFirstChild().getChildNodes();
		Results res = new Results();
		for (int i = 0; i < nodes.getLength(); i++) {
			NodeList childs = nodes.item(i).getChildNodes();

			List<String> row = new LinkedList<>();
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

	private static String readAll(final BufferedReader reader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = reader.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	private static BufferedReader getReader(final String endpoint) throws IOException {
		URL url = new URL(endpoint);
		InputStream stream = url.openStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8")));
		return reader;
	}

	/**
	 * An exact copy of this code is {@link SPARQL#sparql(String)}. Please consider using this, or even {@link ThreadedSPARQL}
	 *
	 * @param service
	 * @param query
	 * @return
	 */
	@Deprecated
	public static Set<RDFNode> sparql(final String service, final String query) {
		Set<RDFNode> set = Sets.newHashSet();

		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		if ((qe != null) && (query.toString() != null)) {
			if (QALD4_EvaluationUtils.isAskType(query)) {
				set.add(new ResourceImpl(String.valueOf(qe.execAsk())));
			} else {
				ResultSet results = qe.execSelect();
				String firstVarName = results.getResultVars().get(0);
				while (results.hasNext()) {

					RDFNode node = results.next().get(firstVarName);
					/**
					 * Instead of returning a set with size 1 and value (null) in it, when no answers are found, this ensures that Set is empty
					 */
					if (node != null) {
						set.add(node);
					}
				}
			}
			qe.close();
		}
		return set;
	}

}
