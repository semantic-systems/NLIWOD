package org.aksw.hawk.pruner;

import java.util.Map;
import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class PredicatesPerVariableEdge implements ISPARQLQueryPruner {

	// PREFIX text:<http://jena.apache.org/text#>
	// SELECT DISTINCT ?proj WHERE {
	// ?const text:query (<http://dbpedia.org/ontology/abstract> 'stage~1'
	// 1000).
	// ?const <http://dbpedia.org/ontology/Film> ?proj.
	// ?const <http://dbpedia.org/ontology/board> ?proj.
	// ?const <http://dbpedia.org/ontology/date> ?proj.
	// }
	// LIMIT 12>
	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, HAWKQuestion q) {
		Set<SPARQLQuery> returnSet = Sets.newHashSet();
		// discard queries with more than x unbound triples away
		for (SPARQLQuery sparqlQuery : queryStrings) {
			Map<String, Set<String>> predicatesPerEdge = Maps.newHashMap();
			String[] split = new String[3];
			boolean flag = true;

			for (String triple : sparqlQuery.constraintTriples) {
				split = triple.split(" ");
				String key = "" + split[0] + split[2];
				if (split[0].startsWith("?") && !split[1].startsWith("?") && split[2].startsWith("?")) {
					if (predicatesPerEdge.containsKey(key)) {
						Set<String> set = predicatesPerEdge.get(key);
						set.add(split[1]);
						predicatesPerEdge.put(key, set);
					} else {
						predicatesPerEdge.put(key, Sets.newHashSet(split[1]));
					}
				}
			}
			// do not allow more than one predicate per edge
			for (String key : predicatesPerEdge.keySet()) {
				if (predicatesPerEdge.get(key).size() > 1) {
					flag = false;
				}
			}
			if (flag) {
				returnSet.add(sparqlQuery);
			}
		}
		return returnSet;
	}

}
