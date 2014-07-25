package org.aksw.hawk.controller;

import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SPARQL {
	Logger log = LoggerFactory.getLogger(SPARQL.class);
	int sizeOfFilterThreshold = 100;

	// TODO treshhold can be increased by introducing prefixes

	public Set<RDFNode> sparql(String query) {
		ArrayList<String> queries = Lists.newArrayList();
		Set<RDFNode> set = Sets.newHashSet();
		QueryExecution qexec = null;
		try {
			if (query.contains("FILTER")) {
				queries = splitLongFilterSPARQL(query);
			} else {
				queries.add(query);
			}
			for (String q : queries) {
				qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", q);
				ResultSet results = qexec.execSelect();
				while (results.hasNext()) {
					set.add(results.next().get("?proj"));
				}
			}
		} catch (Exception e) {
			log.error("Query: " + queries.get(0), e);
		} finally {
			if (qexec != null) {
				qexec.close();
			}
		}
		return set;
	}

	private ArrayList<String> splitLongFilterSPARQL(String query) {
		ArrayList<String> queries = Lists.newArrayList();

		query = query.replaceAll("\n", "");
		Pattern pattern = Pattern.compile(".+FILTER\\s*\\(\\s*\\?proj IN\\s*\\((.+)\\)\\).+");
		Matcher m = pattern.matcher(query);
		log.debug("FILTER Pattern found" + m.find());
		String group = m.group(1);
		query = query.replace(group, "XXAKSWXX");

		String[] uris = group.split(", ");
		for (int i = 0; i < uris.length;) {
			String filter = "";
			for (int sizeOfFilter = 0; sizeOfFilter < sizeOfFilterThreshold && sizeOfFilter + i < uris.length; sizeOfFilter++) {
				filter += uris[i + sizeOfFilter].trim();
				if (sizeOfFilter < (sizeOfFilterThreshold - 1) && sizeOfFilter + i < (uris.length - 1)) {
					filter += ",";
				}
			}
			i += sizeOfFilterThreshold;
			String newQuery = query.replace("XXAKSWXX", filter);
			queries.add(newQuery);
		}
		return queries;
	}

	public static void main(String args[]) {
		String query = "SELECT ?proj WHERE {?proj ?p ?o. FILTER " + "(?proj IN (<http://(1)> , <http://2,3> , <http://3> , <http://4> , <http://5>, <http://6> , <http://7>   ))}";
		SPARQL sqb = new SPARQL();
		ArrayList<String> i = sqb.splitLongFilterSPARQL(query);
		for (String q : i) {
			System.out.println(q);
		}
	}
}
