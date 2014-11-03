/**
 * 
 */
package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.hawk.querybuilding.SPARQLQuery;

/**
 * 
 * @author Lorenz Buehmann
 *
 */
//FIXME convert other query filter to implement this interface
public interface QueryFilter {

	/**
	 * Returns a filtered set of SPARQL queries for a given set of SPARQL queries
	 * based on the filter condition.
	 * @param sparqlQueries the set of SPARQL queries
	 * @return a filtered set of SPARQL queries
	 */
	Set<SPARQLQuery>  filter(Set<SPARQLQuery>  sparqlQueries);

}
