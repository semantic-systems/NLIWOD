package org.aksw.qa.commons.sparql;

import java.util.Set;

import org.aksw.jena_sparql_api.cache.extra.CacheFrontend;
import org.aksw.jena_sparql_api.cache.h2.CacheUtilsH2;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.qa.commons.qald.QALD4_EvaluationUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class SPARQL {
	Logger log = LoggerFactory.getLogger(SPARQL.class);
	public QueryExecutionFactory qef;
	public static final String ENDPOINT_TITAN = "http://139.18.2.164:3030/ds/sparql";
	public static final String ENDPOINT_DBPEIDA_ORG = "http://dbpedia.org/sparql";
	public static final String ENDPOINT_WIKIDATA_ORG = "http://query.wikidata.org/sparql";
	/**
	 * Be sure to imprt the SSL certificate from the metaphacts site to your
	 * local JRE certificate libary:
	 *
	 * http://stackoverflow.com/questions/6659360/how-to-solve-javax-net-ssl-sslhandshakeexception-error
	 * http://superuser.com/questions/97201/how-to-save-a-remote-server-ssl-certificate-locally-as-a-file
	 */
	public static final String ENDPOINT_WIKIDATA_METAPHACTS = "https://wikidata.metaphacts.com/sparql";
	private long timeToLive = 360l * 24l * 60l * 60l * 1000l;

	public SPARQL() {
		try {

			// CacheBackend cacheBackend = CacheCoreH2.create("./sparql",
			// timeToLive, true);
			// CacheFrontend cacheFrontend = new
			// CacheFrontendImpl(cacheBackend);
			CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend("./sparql", true, timeToLive);

			// AKSW SPARQL API call
			// qef = new
			// QueryExecutionFactoryHttp("http://192.168.15.69:8890/sparql",
			// "http://dbpedia.org/");
			// qef = new
			// QueryExecutionFactoryHttp("http://localhost:3030/ds/sparql");
			qef = FluentQueryExecutionFactory.http(ENDPOINT_TITAN).config().withCache(cacheFrontend).end().create();

			// qef = new
			// QueryExecutionFactoryHttp("http://localhost:3030/ds/sparql");
			// qef = new
			// QueryExecutionFactoryHttp("http://dbpedia.org/sparql","http://dbpedia.org");

			// qef = new
			// QueryExecutionFactoryHttp("http://live.dbpedia.org/sparql","http://dbpedia.org");
			// qef = new
			// QueryExecutionFactoryHttp("http://lod.openlinksw.com/sparql/",
			// "http://dbpedia.org");
			// qef = new
			// QueryExecutionFactoryHttp("http://vtentacle.techfak.uni-bielefeld.de:443/sparql",
			// "http://dbpedia.org");
			// --> No reason to be nice
			// qef = new QueryExecutionFactoryDelay(qef, 2000);
			// qef = new QueryExecutionFactoryCacheEx(qef, cacheFrontend);
			// qef = new QueryExecutionFactoryDelay(qef, 150);
			// qef = new QueryExecutionFactoryPaginated(qef, 10000);
		} catch (RuntimeException e) {
			log.error("Could not create SPARQL interface! ", e);
		}
	}

	public SPARQL(final String endpoint) {
		try {
			CacheFrontend cacheFrontend = CacheUtilsH2.createCacheFrontend("./sparql", true, timeToLive);
			qef = FluentQueryExecutionFactory.http(endpoint).config().withCache(cacheFrontend).end().create();
		} catch (RuntimeException e) {
			log.error("Could not create SPARQL interface! ", e);
		}
	}

	/**
	 * using the AKSW library for wrapping Jena API
	 *
	 */
	public Set<RDFNode> sparql(final String query) {
		Set<RDFNode> set = Sets.newHashSet();

		try {
			QueryExecution qe = qef.createQueryExecution(query);
			if ((qe != null) && (query.toString() != null)) {
				if (QALD4_EvaluationUtils.isAskType(query)) {
					set.add(new ResourceImpl(String.valueOf(qe.execAsk())));
				} else {
					ResultSet results = qe.execSelect();
					String firstVarName = results.getResultVars().get(0);
					while (results.hasNext()) {

						RDFNode node = results.next().get(firstVarName);
						/**
						 * Instead of returning a set with size 1 and value
						 * (null) in it, when no answers are found, this ensures
						 * that Set is empty
						 */
						if (node != null) {
							set.add(node);
						}
					}
				}
				qe.close();
			}
		} catch (Exception e) {
			log.error(query.toString(), e);

		}
		return set;
	}

	// /**
	// * Searching varname. For this, find first occurrence of "?" and extract
	// * what comes after that and before next whitespace
	// */
	// public String extractFirstVarname(final String sparqlQuery) {
	// Pattern pattern = Pattern.compile(".+?\\?(\\w+).+", Pattern.DOTALL |
	// Pattern.CASE_INSENSITIVE);
	// Matcher m = pattern.matcher(sparqlQuery);
	// return m.replaceAll("$1");
	// }

	public long getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(final long timeToLive) {
		this.timeToLive = timeToLive;
	}

	// TODO Christian: transform to unit test
	public static void main(final String args[]) {
		SPARQL sqb = new SPARQL();
		// TODO @ricardo from jonathan please take a moment to look at this:

		// TODO In order of generateQueries() to work
		// you need to set something to
		// SparqlQuery.textMapFromVariableToCombinedNNExactMatchToken
		// or to SparqlQuery.textMapFromVariableToSingleFuzzyToken, which are
		// public Maps
		// But for a query with a few basic constraints you wouldnt set
		// something there?! So, only by setting Constraints, generateQueries
		// will always be empty
		SPARQLQuery query = new SPARQLQuery();
		query.addConstraint("?proj a <http://dbpedia.org/ontology/Person>.");
		// query.addConstraint("?proj ?p ?const.");
		// query.addFilter("proj",
		// Lists.newArrayList("http://dbpedia.org/resource/Pope_John_Paul_I",
		// "http://dbpedia.org/resource/Pope_John_Paul_II"));
		// query.addFilter("const",
		// Lists.newArrayList("http://dbpedia.org/resource/Canale_d'Agordo"));

		for (String q : query.generateQueries()) {
			Set<RDFNode> set = sqb.sparql(q);
			for (RDFNode item : set) {
				System.out.println(item);
			}
		}
	}
}
