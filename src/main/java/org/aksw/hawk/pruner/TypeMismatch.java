/**
 * 
 */
package org.aksw.hawk.pruner;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.hawk.pruner.disjointness.QueryUtils;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Filter out queries that contain:
 * 1. triple patterns of type (?s p ?o .) with p not being a property
 * 2. triple patterns of type (?s p ?proj .) with p being a data property
 * @author Lorenz Buehmann
 * 
 */
public class TypeMismatch implements ISPARQLQueryPruner {

	private static final ParameterizedSparqlString typeQueryTemplate = new ParameterizedSparqlString(
			"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#> "
			+ "SELECT ?type WHERE {?s a ?type .}");
	
	private static final Set<Resource> PROPERTY_ENTITY_TYPES = Sets.newHashSet(
			OWL.ObjectProperty, OWL.DatatypeProperty, RDF.Property);

	private static final Logger logger = LoggerFactory.getLogger(TypeMismatch.class);

	private QueryExecutionFactory qef;

	private QueryUtils queryUtils = new QueryUtils();
	
	private Monitor mon = MonitorFactory.getTimeMonitor("typeMismatch");
	
	// properties that are ignored when checking for disjointness
	private static final Set<String> ignoredProperties = Sets.newHashSet(
			"http://jena.apache.org/text#query",
			"http://dbpedia.org/ontology/abstract");

	public TypeMismatch(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.hawk.filtering.QueryFilter#filter(java.util.Set)
	 */
	@Override
	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings) {
		mon.reset();
		Set<SPARQLQuery> filteredQueries = Sets.newHashSet();

		for (SPARQLQuery sparqlQuery : queryStrings) {
			if (accept(sparqlQuery)) {
				filteredQueries.add(sparqlQuery);
			}
		}
		return filteredQueries;
	}

	private Set<Resource> getEntityTypes(String entity) {
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

	private boolean accept(SPARQLQuery sparqlQuery) {
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
				if(predicate.isURI() &&
					!predicate.getNameSpace().equals(RDF.getURI()) && // do not process rdf:
					!predicate.getNameSpace().equals(RDFS.getURI()) && // do not process rdfs:
					!ignoredProperties.contains(predicate.getURI()) // do not process text:
					) {
					
					// predicate is not of type owl:ObjectProperty or owl:DatatypeProperty
					Set<Resource> entityTypes = getEntityTypes(predicate.getURI());
					if(!isProperty(entityTypes)) {
						return false;
					}
					
					// when predicate is a data property, object must not be a project var (holds only for entity queries)
					if(entityTypes.contains(OWL.DatatypeProperty) && projectVars.contains(tp.getObject())){
						return false;
					}
				}
			}
		} finally {
			mon.stop();
		}
		
		return true;
	}
	
	private boolean isProperty(Set<Resource> entityTypes) {
		return !Sets.intersection(entityTypes, PROPERTY_ENTITY_TYPES).isEmpty();
	}

	public static void main(String[] args) {
		QueryExecutionFactory qef = new QueryExecutionFactoryHttp("http://dbpedia.org/sparql", "http://dbpedia.org");
		
		TypeMismatch filter = new TypeMismatch(qef);
		
		SPARQLQuery query1 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query1.addConstraint("?proj <http://dbpedia.org/ontology/author> ?o.");

		SPARQLQuery query2 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query2.addConstraint("?proj <http://dbpedia.org/ontology/Currency> ?o.");
		
		SPARQLQuery query3 = new SPARQLQuery("?s a <http://dbpedia.org/ontology/Book>.");
		query3.addConstraint("?s <http://dbpedia.org/ontology/birthDate> ?proj.");
		
		SPARQLQuery query4 = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Book>.");
		query4.addConstraint("?proj <http://www.w3.org/2000/01/rdf-schema#label> ?label.");
		query4.addConstraint("?label <http://jena.apache.org/text#query> \"'text'\"");

		Set<SPARQLQuery> filtered = filter.prune(Sets.newHashSet(query1, query2, query3, query4));
		System.out.println(filtered);
	}

}
