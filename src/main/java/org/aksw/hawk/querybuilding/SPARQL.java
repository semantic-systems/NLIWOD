package org.aksw.hawk.querybuilding;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SPARQL {
	Logger log = LoggerFactory.getLogger(SPARQL.class);
	// TODO treshold can be increased by introducing prefixes
	int sizeOfFilterThreshold = 50;
	QueryExecutionFactory qef;

	public SPARQL() {
		try {
			// AKSW SPARQL API call
			// QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://live.dbpedia.org/sparql", "http://dbpedia.org");
			qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
			// QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://lod.openlinksw.com/sparql/", "http://dbpedia.org");
			// qef = new QueryExecutionFactoryDelay(qef, 2000); --> No reason to be nice
			long timeToLive = 30l * 24l * 60l * 60l * 1000l;
			CacheCoreEx cacheBackend = CacheCoreH2.create("sparql", timeToLive, true);
			CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
			qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		} catch (ClassNotFoundException | SQLException e) {
			log.error("Could not create SPARQL interface! ", e);
		}
	}

	/**
	 * using the AKSW library for wrapping Jena API
	 * 
	 * @param query
	 * @return
	 */
	public Set<RDFNode> sparql(String query) {
		ArrayList<String> queries = Lists.newArrayList();
		Set<RDFNode> set = Sets.newHashSet();
		QueryExecution qexec = null;
		try {
			if (query.contains("FILTER")) {
				if (query != null) {
					queries = splitLongFilterSPARQL(query);
				}
			} else {
				queries.add(query);
			}
			for (String q : queries) {
				QueryExecution qe = qef.createQueryExecution(q);
				ResultSet results = qe.execSelect();

				while (results.hasNext()) {
					set.add(results.next().get("?proj"));
				}
			}
		} catch (Exception e) {
			log.error("Query: "+ addLinebreaks(query,200),e);

		} finally {
			if (qexec != null) {
				qexec.close();
			}
		}
		return set;
	}

	private String addLinebreaks(String input, int maxLineLength) {
		StringTokenizer tok = new StringTokenizer(input, " ");
		StringBuilder output = new StringBuilder(input.length());
		int lineLen = 0;
		while (tok.hasMoreTokens()) {
			String word = tok.nextToken();
			if (lineLen + word.length() > maxLineLength) {
				output.append("\n");
				lineLen = 0;
			}
			output.append(word + " ");
			lineLen += word.length();
		}
		return output.toString();
	}

	public ArrayList<String> splitLongFilterSPARQL(String query, int threshold) {
		this.sizeOfFilterThreshold = threshold;
		return splitLongFilterSPARQL(query);

	}

	// TODO currently only ?proj gets splitted but not other long filter.
	// think about the right semantics to do so
	
	//TODO build this on top of the new structure of SPARQLQuery object
	private ArrayList<String> splitLongFilterSPARQL(String query) {
		if(query.contains("production")){
			System.out.println();
		}
		ArrayList<String> queries = Lists.newArrayList();
		query = query.replaceAll("\n", "");
		// watch out in URIs could be two closing brackets
		Pattern pattern = Pattern.compile(".+FILTER\\s*\\(\\s*\\?[a-z]+ IN\\s*\\((.+)\\)\\).+");
		Matcher m = pattern.matcher(query);
		log.debug("FILTER Pattern found: " + (m.find() ? true : query));
		String group = m.group(1);
		query = query.replace(group, "XXAKSWXX");

		String[] uris = group.split(", ");
		log.debug("Size of Filter"+ uris.length);
		for (int i = 0; i < uris.length;) {
			String filter = "";
			for (int sizeOfFilter = 0; sizeOfFilter < sizeOfFilterThreshold && sizeOfFilter + i < uris.length; sizeOfFilter++) {
				filter += uris[i + sizeOfFilter].trim();
				if (sizeOfFilter < (sizeOfFilterThreshold - 1) && sizeOfFilter + i < (uris.length - 1)) {
					filter += ", ";
				}
			}
			i += sizeOfFilterThreshold;
			String newQuery = query.replace("XXAKSWXX", filter);
			queries.add(newQuery);
		}
		return queries;
	}

	public static void main(String args[]) {
		String query = "SELECT ?proj WHERE {?proj ?p ?o. "
				+ "FILTER (?proj IN (<http://(1)> , <http://2,3> , <http://3> , <http://4> , <http://5>, <http://6> , <http://61> , <http://62> , <http://7>)). "
				+ "FILTER (?proj IN (<http://(1A)> , <http://2,3> , <http://3> , <http://4B> , <http://5B>, <http://61> , <http://62> , <http://6(X(XY))>)). "
				+ "FILTER (?s IN ( <http://4Bs> , <http://5Bs>, <http://6> , <http://7B>   )). "
				+ " ?s ?p ?oo."
				+ "}";

		SPARQL sqb = new SPARQL();
		ArrayList<String> i = sqb.splitLongFilterSPARQL(query, 2);
		for (String q : i) {
			System.out.println(q);
		}
	}
}
