package hawk;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.pruner.disjointness.DisjointnessBasedQueryFilter;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Sets;

public class DisjointnessBasedQueryFilterTest {


	@Test
		public void test() {

		QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://139.18.2.164:3030/ds/sparql", "http://dbpedia.org");
		DisjointnessBasedQueryFilter filter = new DisjointnessBasedQueryFilter(qef);
		// valid query
		SPARQLQuery query1 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query1.addConstraint("?s <http://dbpedia.org/ontology/author> ?proj.");
		// invalid range
		SPARQLQuery query2 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query2.addConstraint("?o <http://dbpedia.org/ontology/frequency> ?proj.");
		// valid query
		SPARQLQuery query3 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Person>.");
		query3.addConstraint("?proj <http://dbpedia.org/ontology/birthPlace> ?o.");
		// invalid domain
		SPARQLQuery query4 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Person>.");
		query4.addConstraint("?proj <http://dbpedia.org/ontology/frequency> ?o.");
		// Create empty parameter q
		HAWKQuestion q = new HAWKQuestion();
		Set<SPARQLQuery> filtered = filter.prune(Sets.newHashSet(query1, query2, query3, query4), q);
		System.out.println(filtered);

		// two valid queries remaining?
		assertEquals(filtered.size(), 2);

	}

}
