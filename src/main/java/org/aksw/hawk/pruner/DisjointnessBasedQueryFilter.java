/**
 * 
 */
package org.aksw.hawk.pruner;

import java.util.HashSet;
import java.util.Set;

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

/**
 * @author Lorenz Buehmann
 * 
 */
public class DisjointnessBasedQueryFilter implements QueryFilter {

	private static final ParameterizedSparqlString domainQueryTemplate = new ParameterizedSparqlString("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> SELECT ?dom WHERE {?p rdfs:domain ?o . ?o rdfs:subClassOf* ?dom .}");

	private static final ParameterizedSparqlString rangeQueryTemplate = new ParameterizedSparqlString("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> SELECT ?ran WHERE {?p rdfs:range ?o . ?o rdfs:subClassOf* ?ran .}");

	private static final ParameterizedSparqlString superClassesQueryTemplate = new ParameterizedSparqlString("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> SELECT ?sup WHERE {?sub rdfs:subClassOf+ ?sup .}");

	private static final Logger logger = LoggerFactory.getLogger(DisjointnessBasedQueryFilter.class);

	private QueryExecutionFactory qef;

	QueryUtils queryUtils = new QueryUtils();

	public DisjointnessBasedQueryFilter(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.hawk.filtering.QueryFilter#filter(java.util.Set)
	 */
	@Override
	public Set<SPARQLQuery> filter(Set<SPARQLQuery> queryStrings) {
		Set<SPARQLQuery> filteredQueries = Sets.newHashSet();

		for (SPARQLQuery sparqlQuery : queryStrings) {
			if (accept(sparqlQuery)) {
				filteredQueries.add(sparqlQuery);
			}
		}

		return filteredQueries;
	}

	private Set<Node> getDomain(String predicate) {
		Set<Node> domains = new HashSet<Node>();

		domainQueryTemplate.setIri("p", predicate);

		String query = domainQueryTemplate.toString();
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			domains.add(qs.getResource("dom").asNode());
		}
		qe.close();

		return domains;
	}

	private Set<Node> getRange(String predicate) {
		Set<Node> range = new HashSet<Node>();

		rangeQueryTemplate.setIri("p", predicate);

		String query = rangeQueryTemplate.toString();
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			range.add(qs.getResource("ran").asNode());
		}
		qe.close();

		return range;
	}

	private Set<Node> getSuperClasses(String cls) {
		Set<Node> superClasses = new HashSet<Node>();

		superClassesQueryTemplate.setIri("sub", cls);

		String query = superClassesQueryTemplate.toString();
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			superClasses.add(qs.getResource("sup").asNode());
		}
		qe.close();

		return superClasses;
	}

	private boolean accept(SPARQLQuery sparqlQuery) {
		// build Query object
		Query query = QueryFactory.create(sparqlQuery.toString());

		// get all rdf:type triples
		Set<Triple> typeTriples = queryUtils.getRDFTypeTriples(query);

		// get the types for each variable
		Multimap<Var, Node> var2Types = HashMultimap.create();
		for (Triple triple : typeTriples) {
			if (triple.getObject().isURI()) {
				Node type = triple.getObject();
				Var var = Var.alloc(triple.getSubject());
				var2Types.put(var, type);
				// add also the superclasses
				var2Types.putAll(var, getSuperClasses(type.getURI()));
			}
		}

		// get all variables occurring in subject position
		Set<Var> subjectVars = queryUtils.getSubjectVariables(query);

		// check for each variable in subject position if it conflicts with the
		// domain of
		// properties used in other triple patterns with the variable as subject
		for (Var var : subjectVars) {
			Set<Triple> outgoingTriplePatterns = queryUtils.extractOutgoingTriplePatterns(query, var);
			outgoingTriplePatterns.removeAll(typeTriples);

			// check for rdf:type statements
			Set<Node> subjectTypes = (Set<Node>) var2Types.get(var);

			if (!subjectTypes.isEmpty()) {
				// check if the domain of the property is disjoint with any of
				// the types of the subject
				for (Triple tp : outgoingTriplePatterns) {
					Node predicate = tp.getPredicate();
					if (predicate.isURI()&&!predicate.toString().equals("http://dbpedia.org/ontology/abstract")) {
						Set<Node> domain = getDomain(predicate.getURI());
						if (conflicts(subjectTypes, domain)) {
							logger.debug("Domain of " + predicate + " does not match types " + subjectTypes);
							return false;
						}
					}

				}
			}
		}

		// get all variables occurring in object position
		Set<Var> objectVars = queryUtils.getSubjectVariables(query);

		for (Var var : objectVars) {
			Set<Triple> incomingTriplePatterns = queryUtils.extractIncomingTriplePatterns(query, var);

			// check for rdf:type statements
			Set<Node> objectTypes = (Set<Node>) var2Types.get(var);

			if (!objectTypes.isEmpty()) {
				// check if the domain of the property is disjoint with any of
				// the types of the subject
				for (Triple tp : incomingTriplePatterns) {
					Node predicate = tp.getPredicate();
					if (predicate.isURI()) {
						Set<Node> range = getRange(predicate.getURI());
						if (conflicts(objectTypes, range)) {
							logger.debug("Range of " + predicate + " does not match types " + objectTypes);
							return false;
						}
					}

				}
			}
		}
		return true;
	}

	private boolean conflicts(Set<Node> types1, Set<Node> types2) {
		return Sets.intersection(types1, types2).isEmpty();
	}

	public static void main(String[] args) {
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
		DisjointnessBasedQueryFilter filter = new DisjointnessBasedQueryFilter(qef);
		SPARQLQuery query1 = new SPARQLQuery("?s a <http://dbpedia.org/ontology/Book>.");
		query1.addConstraint("?s <http://dbpedia.org/ontology/author> ?o.");

		SPARQLQuery query2 = new SPARQLQuery("?s a <http://dbpedia.org/ontology/Book>.");
		query2.addConstraint("?s <http://dbpedia.org/ontology/birthDate> ?o.");

		Set<SPARQLQuery> filtered = filter.filter(Sets.newHashSet(query1, query2));
		System.out.println(filtered);
	}
}
