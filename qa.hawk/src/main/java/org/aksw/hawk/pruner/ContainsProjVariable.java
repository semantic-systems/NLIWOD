package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;

import com.google.common.collect.Sets;

public class ContainsProjVariable implements ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, HAWKQuestion q) {
		Set<SPARQLQuery> returnList = Sets.newHashSet();
		for (SPARQLQuery query : queryStrings) {
			String[] split = new String[3];

			for (String triple : query.constraintTriples) {
				split = triple.split(" ");
				boolean flag = false;
				if (split[0].equals("?proj")) {
					flag = true;
				}
				if (split[2].equals("?proj.")) {
					flag = true;
				}
				if (flag) {
					returnList.add(query);
				}

			}
		}
		return returnList;
	}
}
