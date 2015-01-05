package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.hawk.pruner.disjointness.DisjointnessBasedQueryFilter;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLQueryPruner implements ISPARQLQueryPruner {
	private static Logger log = LoggerFactory.getLogger(SPARQLQueryPruner.class);
	private DisjointnessBasedQueryFilter disjointness;
	private BGPisConnected BGPisConnected;
	private CyclicTriple cyclicTriple;
	private HasBoundVariables hasBoundVariables;
	private TextFilterOverVariables textFilterOverVariables;
	private UnboundTriple unboundTriple;

	public SPARQLQueryPruner(SPARQL sparql) {
		this.disjointness = new DisjointnessBasedQueryFilter(sparql.qef);

		this.BGPisConnected = new BGPisConnected();
		this.cyclicTriple = new CyclicTriple();
		this.hasBoundVariables = new HasBoundVariables();
		this.textFilterOverVariables = new TextFilterOverVariables();
		this.unboundTriple = new UnboundTriple();

	}

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queries) {

		// Pruning
		log.debug("Number of Queries before pruning: " + queries.size());

		queries = disjointness.prune(queries);
		queries = BGPisConnected.prune(queries);
		queries = cyclicTriple.prune(queries);
		queries = hasBoundVariables.prune(queries);
		queries = textFilterOverVariables.prune(queries);
		queries = unboundTriple.prune(queries);
		
		log.debug("Number of Queries after pruning: " + queries.size());
		// TODO prune things like
		// ?const <http://dbpedia.org/ontology/deathDate> ?proj.
		// ?const <http://dbpedia.org/ontology/deathYear> ?proj.
		return queries;

	}

}
