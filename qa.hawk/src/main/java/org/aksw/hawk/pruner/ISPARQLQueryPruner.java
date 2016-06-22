package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;

public interface ISPARQLQueryPruner {

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queries, HAWKQuestion q);
}
