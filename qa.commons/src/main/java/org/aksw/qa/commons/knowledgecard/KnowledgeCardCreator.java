package org.aksw.qa.commons.knowledgecard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.github.jsonldjava.utils.JsonUtils;

public class KnowledgeCardCreator {

	private static final int MAX_FIELD_SIZE = 5; // Top N Properties that you want
	private static final int API_TIMEOUT = 5000;
	private static final String ENDPOINT = "http://dbpedia.org/sparql";
	
	private static final String PREFIXES = new String("PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
			+ "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + "PREFIX dbo: <http://dbpedia.org/ontology/>\n"
			+ "PREFIX dbp: <http://dbpedia.org/property/>\n" + "PREFIX dbr: <http://dbpedia.org/resource/>\n"
			+ "PREFIX dct: <http://purl.org/dc/terms/>\n");


	private QueryExecution executeQuery(String queryString) {
		Query query = QueryFactory.create(queryString);
		QueryEngineHTTP queryEngine = (QueryEngineHTTP) QueryExecutionFactory.sparqlService(ENDPOINT, query);
		queryEngine.addParam("timeout", String.valueOf(API_TIMEOUT));
		return queryEngine;
	}

	public String process(String uri) throws JsonGenerationException, IOException {
		String query = PREFIXES
				+ "SELECT (GROUP_CONCAT(distinct ?type;separator=' ') as ?types) (GROUP_CONCAT(distinct ?property;separator=' ') as ?properties) WHERE {\n"
				+ "<" + uri + "> rdf:type ?type . FILTER(STRSTARTS(STR(?type), 'http://dbpedia.org/ontology')) . \n"
				+ "<" + uri
				+ "> ?property ?value . FILTER(STRSTARTS(STR(?property), 'http://dbpedia.org/ontology')) . \n" + "}";
		QueryExecution queryExecution = executeQuery(query); // Get all Ontology Classes and Properties for given entity
		Iterator<QuerySolution> results = queryExecution.execSelect();

		LinkedHashSet<Field> fields = new LinkedHashSet<>();

		while (results.hasNext()) {
			QuerySolution solution = results.next();
			if (solution.get("types") != null && solution.get("properties") != null) {
				List<String> types = Arrays.asList(solution.get("types").asLiteral().getString().split(" "));
				String[] split = solution.get("properties").asLiteral().getString().split(" ");
				HashSet<String> properties = Sets
						.newHashSet(split);
				// Get Relevant Properties based on CouchDB
				List<Field> relevantProperties = getRelevantProperties(uri, types, properties);
				fields.addAll(relevantProperties);
			}
		}

		queryExecution.close();
		
		
		return JsonUtils.toPrettyString(fields);
	}

	public List<Field> getRelevantProperties(String uri, List<String> Answer, HashSet<String> properties) {
		List<Field> fields = new ArrayList<Field>();
		try {
			TreeMap<Float, String> propertyMap = new TreeMap<Float, String>();
			List<ExplorerProperties> explorerProperties = readCSVWithExplorerProperties(properties);

			for (ExplorerProperties property : explorerProperties) {
				// Check if the property matches one of the list of classes(types) found for the entity
				if (Answer.contains(property.getClassName())) {
					propertyMap.put(Float.parseFloat(property.getScore()), property.getProperty());
				}
			}
			if (propertyMap.size() > 0) {
				int count = 0;
				Iterator<Float> iterator = propertyMap.descendingKeySet().iterator(); // Sorts descending order
				String property_uris = "";
				while (count < MAX_FIELD_SIZE && iterator.hasNext()) {
					property_uris += "<" + propertyMap.get(iterator.next()) + "> ";
					count++;
				}

				String query = PREFIXES
						+ "SELECT ?property_label (group_concat(distinct ?value;separator='__') as ?values) (group_concat(distinct ?value_label;separator='__') as ?value_labels) where {\n"
						+ "VALUES ?property {" + property_uris + "}\n" + "<" + uri + "> ?property ?value . \n"
						+ "?property rdfs:label ?property_label . FILTER(lang(?property_label)='en'). \n"
						+ "OPTIONAL {?value rdfs:label ?value_label . FILTER(lang(?value_label) = 'en') }\n"
						+ "} GROUP BY ?property_label";
				QueryExecution queryExecution = executeQuery(query);
				try {
					Iterator<QuerySolution> results = queryExecution.execSelect();
					while (results.hasNext()) {
						QuerySolution result = results.next();
						Field field = new Field();
						field.setName(result.get("property_label").asLiteral().getString());

						// If Value Label String is empty then we use Value String instead which means
						// the value is a literal. So we are only taking the first element before space
						if (result.get("value_labels").asLiteral().getString().equals("")) {
							field.setValue(result.get("values").asLiteral().getString().split("__")[0]);
						} else {
							LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
							String[] keyArray = result.get("values").asLiteral().getString().split("__");
							String[] valueArray = result.get("value_labels").asLiteral().getString().split("__");

							for (int index = 0; index < keyArray.length; index++) {
								map.put(keyArray[index], valueArray[index]);
							}
							field.setValues(map);
						}
						fields.add(field);
					}
					return fields;
				} finally {
					queryExecution.close();
				}
			}
			return fields;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return fields;
	}

	private List<ExplorerProperties> readCSVWithExplorerProperties(HashSet<String> properties) throws IOException {
	    InputStream openResource = this.getClass().getClassLoader().getResourceAsStream("db.csv");
		InputStreamReader in = new InputStreamReader(openResource);
		BufferedReader br = new BufferedReader(in);
		List<ExplorerProperties> tmp = new ArrayList<ExplorerProperties>();
		while (br.ready()) {
			String[] line = br.readLine().split(",");
			if (properties.contains(line[1])) {
				tmp.add(new ExplorerProperties(line[0], line[1], line[2]));
			}
		}
		br.close();
		return tmp;
	}
}