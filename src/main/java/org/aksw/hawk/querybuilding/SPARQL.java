package org.aksw.hawk.querybuilding;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreEx;
import org.aksw.jena_sparql_api.cache.extra.CacheCoreH2;
import org.aksw.jena_sparql_api.cache.extra.CacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheExImpl;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.pagination.core.QueryExecutionFactoryPaginated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SPARQL {
	Logger log = LoggerFactory.getLogger(SPARQL.class);
	QueryExecutionFactory qef;

	public SPARQL() {
		try {
			long timeToLive = 30l * 24l * 60l * 60l * 1000l;
			CacheCoreEx cacheBackend = CacheCoreH2.create("sparql", timeToLive, true);
			CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
			// AKSW SPARQL API call
			// qef = new QueryExecutionFactoryHttp("http://live.dbpedia.org/sparql", "http://dbpedia.org");
			qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");//
			// qef = new QueryExecutionFactoryHttp("http://lod.openlinksw.com/sparql/", "http://dbpedia.org");
			// --> No reason to be nice
			// qef = new QueryExecutionFactoryDelay(qef, 2000);
			qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
			qef = new QueryExecutionFactoryPaginated(qef, 20000);
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
	public Set<RDFNode> sparqlWithFilterOnServerSite(SPARQLQuery query) {
		Set<RDFNode> set = Sets.newHashSet();
		try {
			QueryExecution qe = qef.createQueryExecution(query.toStringWithoutFilter());
			ResultSet results = qe.execSelect();
			while (results.hasNext()) {
				QuerySolution next = results.next();
				boolean addToFinalSet = true;
				for (String var : results.getResultVars()) {
					RDFNode valueFromEndpoint = next.get(var);
					List<String> valuesFromGeneratedQuery = query.filter.get(var);
					// filter current URI with URI list from generated query
					if (!valuesFromGeneratedQuery.contains(valueFromEndpoint.toString())) {
						addToFinalSet = false;
						break;
					}
				}
				if (addToFinalSet) {
					set.add(next.get("proj"));
				}
			}
		} catch (Exception e) {
			log.error("Query: " + addLinebreaks(query.toStringWithoutFilter(), 200), e);
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

	public static void main(String args[]) {

		SPARQL sqb = new SPARQL();

		SPARQLQuery query = new SPARQLQuery();
		query.addConstraint("?proj a <http://dbpedia.org/ontology/Cleric>.");
		query.addConstraint("?proj ?p ?const.");
		query.addFilter("proj", Lists.newArrayList("http://dbpedia.org/resource/Pope_John_Paul_I", "http://dbpedia.org/resource/Pope_John_Paul_II"));
		query.addFilter("const", Lists.newArrayList("http://dbpedia.org/resource/Canale_d'Agordo"));

		Set<RDFNode> set = sqb.sparqlWithFilterOnServerSite(query);
		for (RDFNode item : set) {
			System.out.println(item);
		}
	}
}
