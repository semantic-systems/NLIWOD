/**
 * 
 */
package org.aksw.hawk.pruner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.pruner.disjointness.QueryUtils;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Filter out queries that contain: 1. triple patterns of type (?s p ?o .) with
 * p not being a property 2. triple patterns of type (?s p ?proj .) with p being
 * a data property
 * 
 * @author Lorenz Buehmann
 * 
 */
public class TypeMismatch implements ISPARQLQueryPruner {

	private static final ParameterizedSparqlString typeQueryTemplate = new ParameterizedSparqlString("PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> " + "SELECT ?type WHERE {?s a ?type .}");

	private static final Set<Resource> PROPERTY_ENTITY_TYPES = Sets.newHashSet(OWL.ObjectProperty, OWL.DatatypeProperty, RDF.Property);

	private static final Logger logger = LoggerFactory.getLogger(TypeMismatch.class);

	private QueryExecutionFactory qef;

	private QueryUtils queryUtils = new QueryUtils();

	private Monitor mon = MonitorFactory.getTimeMonitor("typeMismatch");

	// properties that are ignored when checking for disjointness
	private static final Set<String> ignoredProperties = Sets.newHashSet("http://jena.apache.org/text#query", "http://dbpedia.org/ontology/abstract");

	public TypeMismatch(final QueryExecutionFactory qef) {
		this.qef = qef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.hawk.filtering.QueryFilter#filter(java.util.Set)
	 */

	@Override
	public Set<SPARQLQuery> prune(final Set<SPARQLQuery> queryStrings, final HAWKQuestion q) {
		mon.reset();
		Set<SPARQLQuery> filteredQueries = Sets.newHashSet();

		for (SPARQLQuery sparqlQuery : queryStrings) {
			if (accept(sparqlQuery)) {
				filteredQueries.add(sparqlQuery);
			}
		}
		return filteredQueries;
	}

	private Set<Resource> getEntityTypes(final String entity) {
		Set<Resource> entityTypes = new HashSet<>();
		typeQueryTemplate.setIri("s", entity);

		String query = typeQueryTemplate.toString();
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			Resource type = qs.getResource("type");
			entityTypes.add(type);
		}
		qe.close();
		return entityTypes;
	}

	private boolean accept(final SPARQLQuery sparqlQuery) {
		mon.start();

		try {
			// build query object
			Query query = QueryFactory.create(sparqlQuery.toString());

			// get project var
			List<Var> projectVars = query.getProjectVars();

			// get all triple patterns
			Set<Triple> triplePatterns = queryUtils.extractTriplePattern(query);

			// check for each triple pattern
			for (Triple tp : triplePatterns) {

				// checks based on predicate
				Node predicate = tp.getPredicate();
				if (predicate.isURI() && !predicate.getNameSpace().equals(RDF.getURI()) && // do
				                                                                           // not
				                                                                           // process
				                                                                           // rdf:
				        !predicate.getNameSpace().equals(RDFS.getURI()) && // do
				                                                           // not
				                                                           // process
				                                                           // rdfs:
				        !ignoredProperties.contains(predicate.getURI()) // do
				                                                        // not
				                                                        // process
				                                                        // text:
				) {

					// predicate is not of type owl:ObjectProperty or
					// owl:DatatypeProperty
					Set<Resource> entityTypes = getEntityTypes(predicate.getURI());
					if (!isProperty(entityTypes)) {
						return false;
					}

					// when predicate is a data property, object must not be a
					// project var (holds only for entity queries)
					if (entityTypes.contains(OWL.DatatypeProperty) && projectVars.contains(tp.getObject())) {
						return false;
					}
				}
			}
		} finally {
			mon.stop();
		}

		return true;
	}

	private boolean isProperty(final Set<Resource> entityTypes) {
		return !Sets.intersection(entityTypes, PROPERTY_ENTITY_TYPES).isEmpty();
	}

}