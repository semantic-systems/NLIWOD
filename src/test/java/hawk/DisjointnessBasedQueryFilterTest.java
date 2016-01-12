package hawk;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.pruner.ISPARQLQueryPruner;
import org.aksw.hawk.pruner.disjointness.DisjointnessBasedQueryFilter;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class DisjointnessBasedQueryFilterTest {

	@Test
	public void test() {

		QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
		DisjointnessBasedQueryFilter filter = new DisjointnessBasedQueryFilter(qef);
		//valid query
		SPARQLQuery query1 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query1.addConstraint("?s <http://dbpedia.org/ontology/author> ?proj.");
		//invalid range
		SPARQLQuery query2 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query2.addConstraint("?o <http://dbpedia.org/ontology/frequency> ?proj.");
		//valid query
		SPARQLQuery query3 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Person>.");
		query3.addConstraint("?proj <http://dbpedia.org/ontology/birthPlace> ?o.");
		//invalid domain
		SPARQLQuery query4 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Person>.");
		query4.addConstraint("?proj <http://dbpedia.org/ontology/frequency> ?o.");
		//Create empty parameter q
		Question q=new Question();
		Set<SPARQLQuery> filtered = filter.prune(Sets.newHashSet(query1, query2, query3, query4), q);
		System.out.println(filtered);

		//two valid queries remaining?
		assertEquals(filtered.size(),2);

	}

}
