package org.aksw.hawk;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.pruner.SPARQLQueryPruner;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class SpecificQueryTest {

	@BeforeClass
	public static void checkServer() {
		if (!ServerChecks.titanSparqlAlive()) {
			throw new Error("Server down");
		}
	}

	Logger log = LoggerFactory.getLogger(SpecificQueryTest.class);
	SPARQL sparql = new SPARQL();
	SPARQLQueryPruner pruner = new SPARQLQueryPruner(sparql);

	@Test
	public void specificQuery() {

		// PREFIX text: <http://jena.apache.org/text#>
		// SELECT DISTINCT ?proj WHERE {
		// ?proj text:query (<http://dbpedia.org/ontology/abstract> '"Coquette
		// Productions"' 1000).
		// ?const <http://dbpedia.org/ontology/starring> ?proj.
		// }
		// LIMIT 12
		Set<SPARQLQuery> queries = Sets.newHashSet();
		SPARQLQuery query = new SPARQLQuery("?const <http://dbpedia.org/ontology/starring> ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "Coquette Productions");
		log.debug(query.toString());
		queries.add(query);

		// PREFIX text:<http://jena.apache.org/text#>
		// SELECT DISTINCT ?proj WHERE {
		// ?const text:query (<http://dbpedia.org/ontology/abstract> 'girls~1
		// AND Xposé~1' 1000).
		// ?proj <http://dbpedia.org/ontology/residence> ?const.
		// }
		// LIMIT 12>
		query = new SPARQLQuery("?proj  <http://dbpedia.org/ontology/residence> ?const.");
		query.addFilterOverAbstractsContraint("?const", "girls Xposé leader");
		log.debug(query.toString());
		queries.add(query);

		// PREFIX text: <http://jena.apache.org/text#>
		// SELECT DISTINCT ?proj WHERE {
		// ?proj text:query (<http://dbpedia.org/ontology/abstract> 'Wu~1 AND
		// Clan~1 AND stage~1 AND Tang~1' 1000).
		// ?const ?proot ?proj.
		// ?const a <http://dbpedia.org/ontology/Film>.
		// }
		// LIMIT 12

		query = new SPARQLQuery("?const ?proot ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "Wu Tang Clan");
		query.addConstraint("?const a <http://dbpedia.org/ontology/Film>.");
		log.debug(query.toString());
		queries.add(query);

		// PREFIX text:<http://jena.apache.org/text#>
		// SELECT DISTINCT ?proj WHERE {
		// ?const text:query (<http://dbpedia.org/ontology/abstract> '"first man
		// in space"' 1000).
		// ?const <http://dbpedia.org/ontology/deathPlace> ?proj.
		// ?proj a <http://dbpedia.org/ontology/Place>.
		// }
		// LIMIT 1
		query = new SPARQLQuery("?const <http://dbpedia.org/ontology/deathPlace> ?proj.");
		query.addFilterOverAbstractsContraint("?const", "first man in space");
		log.debug(query.toString());
		queries.add(query);

		// PREFIX text: <http://jena.apache.org/text#>
		// SELECT DISTINCT ?proj WHERE {
		// ?const text:query (<http://dbpedia.org/ontology/abstract>
		// 'assassin~1' 1000).
		// ?const <http://dbpedia.org/ontology/birthPlace> ?proj.
		// }
		// LIMIT 12

		query = new SPARQLQuery("?const <http://dbpedia.org/ontology/birthPlace> ?proj.");
		query.addFilterOverAbstractsContraint("?const", "assassin");
		log.debug(query.toString());
		queries.add(query);

		log.debug("Size before pruning: " + queries.size());
		queries = pruner.prune(queries, new HAWKQuestion());
		log.debug("Size after pruning: " + queries.size());
		Assert.assertTrue(queries.size() == 5);

	}

}
