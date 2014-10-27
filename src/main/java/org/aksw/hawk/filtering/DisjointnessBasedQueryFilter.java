/**
 * 
 */
package org.aksw.hawk.filtering;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.vocabulary.RDF;

/**
 * @author Lorenz Buehmann
 *
 */
public class DisjointnessBasedQueryFilter implements QueryFilter {
	
	private static final ParameterizedSparqlString domainQueryTemplate = new ParameterizedSparqlString(
			"SELECT ?dom WHERE {?p rdfs:domain ?dom .}");
	
	private static final ParameterizedSparqlString rangeQueryTemplate = new ParameterizedSparqlString(
			"SELECT ?ran WHERE {?p rdfs:range ?ran .}");
	
	private static final Logger logger = LoggerFactory.getLogger(DisjointnessBasedQueryFilter.class);
	
	private QueryExecutionFactory qef;
	
	QueryUtils queryUtils = new QueryUtils();

	public DisjointnessBasedQueryFilter(QueryExecutionFactory qef) {
		this.qef = qef;
	}

	/* (non-Javadoc)
	 * @see org.aksw.hawk.filtering.QueryFilter#filter(java.util.Set)
	 */
	@Override
	public Set<String> filter(Set<String> sparqlQueries) {
		Set<String> filteredQueries = new HashSet<String>(sparqlQueries.size());
		
		for (String sparqlQuery : sparqlQueries) {
			
		}
		
		return filteredQueries;
	}
	
	private Set<String> getDomains(String predicate){
		Set<String> domains = new HashSet<String>();
		
		domainQueryTemplate.setIri("p", predicate);
		
		String query = domainQueryTemplate.toString();
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()){
			QuerySolution qs = rs.next();
			domains.add(qs.getResource("dom").getURI());
		}
		qe.close();
		
		return domains;
	}
	
	private Set<String> getRange(String predicate){
		Set<String> range = new HashSet<String>();
		
		rangeQueryTemplate.setIri("p", predicate);
		
		String query = domainQueryTemplate.toString();
		QueryExecution qe = qef.createQueryExecution(query);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()){
			QuerySolution qs = rs.next();
			range.add(qs.getResource("dom").getURI());
		}
		qe.close();
		
		return range;
	}
	
	private boolean accept(String sparqlQuery){
		// build Query object
		Query query = QueryFactory.create(sparqlQuery);
		
		// get all variables occurring in subject position
		Set<Var> subjectVars = queryUtils.getSubjectVariables(query);
		
		for (Var var : subjectVars) {
			Set<Triple> outgoingTriplePatterns = queryUtils.extractOutgoingTriplePatterns(query, var);
			
			// check for rdf:type statements
			Set<Node> subjectTypes = new HashSet<Node>();
			for (Iterator<Triple> iter = outgoingTriplePatterns.iterator(); iter.hasNext(); ) {
				Triple tp = iter.next();
				if(tp.getPredicate().matches(RDF.type.asNode()) && tp.getObject().isURI()){
					subjectTypes.add(tp.getObject());
					iter.remove();
				}
			}
			if(!subjectTypes.isEmpty()) {
				// check if the domain of the property is disjoint with any of the types of the subject
				for (Triple tp : outgoingTriplePatterns) {
					Node predicate = tp.getPredicate();
					
				}
			}
		}
		return true;
	}

}
