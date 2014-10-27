/**
 * 
 */
package org.aksw.hawk.filtering;

import java.util.Set;

/**
 * 
 * @author Lorenz Buehmann
 *
 */
public interface QueryFilter {

	/**
	 * Returns a filtered set of SPARQL queries for a given set of SPARQL queries
	 * based on the filter condition.
	 * @param sparqlQueries the set of SPARQL queries
	 * @return a filtered set of SPARQL queries
	 */
	Set<String> filter(Set<String> sparqlQueries);
}
