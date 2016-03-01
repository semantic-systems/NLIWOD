package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;

import com.google.common.collect.Sets;

public class NumberOfTypesPerVariable implements ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, HAWKQuestion q) {
		Set<SPARQLQuery> returnSet = Sets.newHashSet();
		for (SPARQLQuery sparqlQuery : queryStrings) {
			String[] split = new String[3];
			Set<String> variableWithType = Sets.newHashSet();
			boolean flag = true;
			for (String triple : sparqlQuery.constraintTriples) {
				split = triple.split(" ");
				if (split[0].startsWith("?") && split[1].equals("a")) {
					if (variableWithType.contains(split[0])) {
						flag = false;
					} else {
						variableWithType.add(split[0]);
					}
				}
			}

			if (flag) {
				returnSet.add(sparqlQuery);
			}
		}
		return returnSet;
	}
}
