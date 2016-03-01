package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;

import com.google.common.collect.Sets;

public class ContainsTooManyNodesAsTextLookUp implements ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, HAWKQuestion q) {
		Set<SPARQLQuery> returnList = Sets.newHashSet();
		for (SPARQLQuery query : queryStrings) {
			// assume only one variable left
			for (String variable : query.textMapFromVariableToCombinedNNExactMatchToken.keySet()) {
				if (query.textMapFromVariableToCombinedNNExactMatchToken.get(variable).size() <= 2) {
					returnList.add(query);
				}
			}
		}
		return returnList;
	}
}
