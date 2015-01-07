package hawk;

import java.util.Set;

import org.aksw.hawk.pruner.SPARQLQueryPruner;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class SpecificQueryTest {
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
		
		
//		PREFIX text:<http://jena.apache.org/text#> 
//			SELECT DISTINCT ?proj WHERE {
//			 ?const text:query (<http://dbpedia.org/ontology/abstract> 'girls~1 AND Xposé~1' 1000). 
//			?proj  <http://dbpedia.org/ontology/residence> ?const. 
//			}
//			LIMIT 12>
		query = new SPARQLQuery("?proj  <http://dbpedia.org/ontology/residence> ?const.");
		query.addFilterOverAbstractsContraint("?const", "girls Xposé leader");
		log.debug(query.toString());
		queries.add(query);

		log.debug("Size before pruning: " + queries.size());
		queries = pruner.prune(queries);
		log.debug("Size after pruning: " + queries.size());
		Assert.assertTrue(queries.size() == 2);

	}

}
