package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.pruner.disjointness.DisjointnessBasedQueryFilter;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

	@SuppressWarnings("unchecked")
	// TODO make this login / JSON more pretty
	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queries, Question q) {

		JSONArray document = new JSONArray();
		// Pruning
		int initialQueriesNumber = queries.size();
		JSONObject tmp = new JSONObject();
		tmp.put("label", "Number of Queries before pruning");
		tmp.put("value", initialQueriesNumber);
		document.add(tmp);

		// this pruning should be first since it assures valid queries for the
		// following steps
		queries = underdefined.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "Underdefined pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = containsProjVariable.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries containing no project variable pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = containsTooManyNodesAsTextLookUp.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries containing too many nodes as text lookup pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = predicatesPerVariableEdge.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with more than one predicate between the same variables pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = numberOfTypesPerVariable.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with more than one type per variable pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = BGPisConnected.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries without connected BGP pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = cyclicTriple.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries containing cycic triple pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = hasBoundVariables.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries without bound variables pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = textFilterOverVariables.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries without text filter over existing variables pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = unboundTriple.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with unbound triples pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = typemismatch.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with mismatching types pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		queries = disjointness.prune(queries,q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with disjoint classes pruned");
		tmp.put("value", (initialQueriesNumber - queries.size()));
		document.add(tmp);
		initialQueriesNumber = queries.size();

		tmp = new JSONObject();
		tmp.put("label", "Number of Queries after pruning");
		tmp.put("value", queries.size());
		document.add(tmp);
		
		q.pruning_messages = document;
		log.debug(document.toJSONString());
		// TODO prune things like
		// ?const <http://dbpedia.org/ontology/deathDate> ?proj.
		// ?const <http://dbpedia.org/ontology/deathYear> ?proj.
		return queries;

	}

}
