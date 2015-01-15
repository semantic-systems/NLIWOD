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
	private UnderDefinedQueries underdefined;
	private PredicatesPerVariableEdge predicatesPerVariableEdge;
	private NumberOfTypesPerVariable numberOfTypesPerVariable;
	private ContainsProjVariable containsProjVariable;
	private ContainsTooManyNodesAsTextLookUp containsTooManyNodesAsTextLookUp;
	private TypeMismatch typemismatch;

	public SPARQLQueryPruner(SPARQL sparql) {
		this.disjointness = new DisjointnessBasedQueryFilter(sparql.qef);

		this.BGPisConnected = new BGPisConnected();
		this.cyclicTriple = new CyclicTriple();
		this.hasBoundVariables = new HasBoundVariables();
		this.textFilterOverVariables = new TextFilterOverVariables();
		this.unboundTriple = new UnboundTriple();
		this.underdefined = new UnderDefinedQueries();
		this.predicatesPerVariableEdge = new PredicatesPerVariableEdge();
		this.numberOfTypesPerVariable = new NumberOfTypesPerVariable();
		this.containsProjVariable = new ContainsProjVariable();
		this.containsTooManyNodesAsTextLookUp = new ContainsTooManyNodesAsTextLookUp();
		this.typemismatch = new TypeMismatch(sparql.qef);

	}

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queries) {

		// Pruning
		int initialQueriesNumber = queries.size();
		log.debug("Number of Queries before pruning: " + initialQueriesNumber);

		// this pruning should be first since it assures valid queries for the
		// following steps
		queries = underdefined.prune(queries);
		log.debug("underdefined pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = containsProjVariable.prune(queries);
		log.debug("containsProjVariable pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = containsTooManyNodesAsTextLookUp.prune(queries);
		log.debug("containsTooManyNodesAsTextLookUp pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = predicatesPerVariableEdge.prune(queries);
		log.debug("predicatesPerVariableEdge pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = numberOfTypesPerVariable.prune(queries);
		log.debug("numberOfTypesPerVariable pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = BGPisConnected.prune(queries);
		log.debug("BGPisConnected pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = cyclicTriple.prune(queries);
		log.debug("cyclicTriple pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = hasBoundVariables.prune(queries);
		log.debug("hasBoundVariables pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = textFilterOverVariables.prune(queries);
		log.debug("textFilterOverVariables pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = unboundTriple.prune(queries);
		log.debug("unboundTriple pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = typemismatch.prune(queries);
		log.debug("typemismatch pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		queries = disjointness.prune(queries);
		log.debug("disjointness pruned: " + (initialQueriesNumber - queries.size()));
		initialQueriesNumber = queries.size();

		log.debug("Number of Queries after pruning: " + queries.size());
		// TODO prune things like
		// ?const <http://dbpedia.org/ontology/deathDate> ?proj.
		// ?const <http://dbpedia.org/ontology/deathYear> ?proj.
		return queries;

	}

}
