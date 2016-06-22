package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;

import com.google.common.collect.Sets;

public class HasBoundVariables implements ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, HAWKQuestion q) {
		Set<SPARQLQuery> returnList = Sets.newHashSet();
		for (SPARQLQuery queryString : queryStrings) {
			boolean flag = true;
			for (String triple : queryString.constraintTriples) {
				if (triple.contains("http")) {
					flag = true;
				}
			}
			// if (queryString.filter.isEmpty()) {
			// flag = false;
			// }
			if (flag) {
				returnList.add(queryString);
			}
		}
		return returnList;
	}
}
