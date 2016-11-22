package org.aksw.hawk;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.pruner.TypeMismatch;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.qa.commons.sparql.SPARQLQuery;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Sets;

public class TypeMismatchTest {

	@Test
	@Ignore
	//TODO ask Lorenz Bühmann why this is 1 but should be 2
	public void test() {

		QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://139.18.2.164:3030/ds/sparql", "http://dbpedia.org");

		TypeMismatch filter = new TypeMismatch(qef);
		// Matching Query
		SPARQLQuery query1 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query1.addConstraint("?proj <http://dbpedia.org/ontology/author> ?o.");
		// Mismatched Query
		SPARQLQuery query2 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query2.addConstraint("?proj <http://dbpedia.org/ontology/Currency> ?o.");
		// Mismatched Query
		SPARQLQuery query3 = new SPARQLQuery("?s a <http://dbpedia.org/ontology/Book>.");
		query3.addConstraint("?s <http://dbpedia.org/ontology/birthDate> ?proj.");
		// Matching query
		SPARQLQuery query4 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query4.addConstraint("?proj <http://www.w3.org/2000/01/rdf-schema#label> ?label.");
		query4.addConstraint("?label <http://jena.apache.org/text#query> \"'text'\"");
		// Fills unused parameter Question q
		HAWKQuestion q = new HAWKQuestion();
		Set<SPARQLQuery> filtered = filter.prune(Sets.newHashSet(query1, query2, query3, query4), q);
		// Two valid queries remaining?
		assertEquals(filtered.size(), 2);
	}

}
