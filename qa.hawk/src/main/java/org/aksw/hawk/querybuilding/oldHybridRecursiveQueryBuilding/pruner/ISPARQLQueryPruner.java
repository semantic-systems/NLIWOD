package org.aksw.hawk.querybuilding.oldHybridRecursiveQueryBuilding.pruner;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.sparql.SPARQLQuery;

public interface ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queries, HAWKQuestion q);
}
