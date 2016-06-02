package org.aksw.hawk.util;

import java.sql.SQLException;
import java.util.Set;

import org.aksw.jena_sparql_api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.jena_sparql_api.cache.extra.CacheBackend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.extra.CacheFrontendImpl;
import org.aksw.jena_sparql_api.cache.h2.CacheCoreH2;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.atlas.json.JsonString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * Creates thumbnail, abstract, comment, label per URI
 * 
 * Does only work if DBpedia is working
 * 
 * @author ricardousbeck
 *
 */
public class AnswerBox {
	static Logger log = LoggerFactory.getLogger(AnswerBox.class);
	private static QueryExecutionFactory qef;

	static {
		try {
			long timeToLive = 360l * 24l * 60l * 60l * 1000l;
			CacheBackend cacheBackend = CacheCoreH2.create("./sparql", timeToLive, true);
			CacheFrontend cacheFrontend = new CacheFrontendImpl(cacheBackend);
			// CacheCoreEx cacheBackend = CacheCoreH2.create("./sparql",
			// timeToLive, true);
			// CacheEx cacheFrontend = new CacheExImpl(cacheBackend);
			qef = new QueryExecutionFactoryHttp("http://live.dbpedia.org/sparql");
			qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
		} catch (ClassNotFoundException | SQLException e) {
			log.error("Could not create SPARQL interface! ", e);
		}
	}

	public static JSONObject buildAnswerBoxFeatures(final String uri) {
		JSONObject document = new JSONObject();
		document.put("URI", new JsonString(uri));
		Set<RDFNode> set = Sets.newHashSet();
		try {
			String query = "select ?thumbnail ?abstract ?comment ?label" + "where {" + "<" + uri + "> <http://dbpedia.org/ontology/thumbnail> ?thumbnail;"
			        + "<http://dbpedia.org/ontology/abstract> ?abstract;" + "<http://www.w3.org/2000/01/rdf-schema#label> ?label;" + "<http://www.w3.org/2000/01/rdf-schema#comment> ?comment."
			        + "FILTER(langMatches(lang(?abstract), \"EN\") &&" + "          langMatches(lang(?label), \"EN\") &&" + "          langMatches(lang(?comment), \"EN\"))" + "}";
			QueryExecution qe = qef.createQueryExecution(query);
			if (qe != null && query.toString() != null) {
				ResultSet results = qe.execSelect();
				while (results.hasNext()) {
					QuerySolution next = results.next();
					RDFNode thumbnail = next.get("thumbnail");
					RDFNode abstractLiteral = next.get("abstract");
					RDFNode commentLiteral = next.get("comment");
					RDFNode labelLiteral = next.get("label");
					if (thumbnail != null) {
						document.put("thumbnail", new JsonString(thumbnail.asResource().getURI()));
					}
					if (abstractLiteral != null) {
						document.put("abstract", new JsonString(abstractLiteral.asLiteral().getString()));
					}
					if (commentLiteral != null) {
						document.put("comment", new JsonString(commentLiteral.asLiteral().getString()));
					}
					if (labelLiteral != null) {
						document.put("label", new JsonString(labelLiteral.asLiteral().getString()));
					}

				}
			}
		} catch (Exception e) {
			log.error("Cannot ask DBpedia for verbose description of " + uri, e);
		}
		return document;
	}
}
