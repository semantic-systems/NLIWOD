package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;

import com.google.common.collect.Sets;

public class UnderDefinedQueries implements ISPARQLQueryPruner {

	/*
	 * underdefined queries cause a high computational effort without resulting
	 * in highly precise resultsets For example: PREFIX
	 * text:<http://jena.apache.org/text#> SELECT DISTINCT ?proj WHERE { ?proj
	 * text:query (<http://dbpedia.org/ontology/abstract> '"overlook"' 1000).
	 * ?const ?proot ?proj. ?const a <http://dbpedia.org/ontology/Building>. }
	 * 
	 * or
	 * 
	 * PREFIX text:<http://jena.apache.org/text#> SELECT DISTINCT ?proj WHERE {
	 * ?const text:query (<http://dbpedia.org/ontology/abstract> 'overlook~1'
	 * 1000). ?proj a <http://dbpedia.org/ontology/ReligiousBuilding>. ?const
	 * ?proot ?proj. ?proj ?pbridge <http://dbpedia.org/resource/North_Sea>. }
	 * LIMIT 12
	 */
	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, HAWKQuestion q) {
		Set<SPARQLQuery> returnSet = Sets.newHashSet();
		for (SPARQLQuery sparqlQuery : queryStrings) {

			// PREFIX text:<http://jena.apache.org/text#>
			// SELECT DISTINCT ?proj WHERE {
			// }
			// LIMIT 12
			if (sparqlQuery.constraintTriples.isEmpty() && sparqlQuery.textMapFromVariableToCombinedNNExactMatchToken.isEmpty()) {
				continue;
			}
			String[] split = new String[3];
			boolean containsOnlyUnboundTriple = true;
			for (String triple : sparqlQuery.constraintTriples) {
				split = triple.split(" ");
				if (!split[0].startsWith("?") || !split[1].startsWith("?") || !split[2].startsWith("?")) {
					containsOnlyUnboundTriple = false;
				}
			}
			boolean containsOnlyTypeDefinitions = true;
			for (String triple : sparqlQuery.constraintTriples) {
				split = triple.split(" ");
				if (!split[1].equals("a")) {
					containsOnlyTypeDefinitions = false;
				}
			}
			boolean wellDefinedTextFilter = false;
			// assuming here are only queries left by pruning containing only a
			// text filter over one query
			for (String key : sparqlQuery.textMapFromVariableToSingleFuzzyToken.keySet()) {
				if (sparqlQuery.textMapFromVariableToSingleFuzzyToken.get(key).size() >= 2) {
					wellDefinedTextFilter = true;
				}
			}
			if ((!containsOnlyUnboundTriple && !containsOnlyTypeDefinitions) || (containsOnlyTypeDefinitions && wellDefinedTextFilter)) {
				returnSet.add(sparqlQuery);
			}
		}
		return returnSet;
	}
}
