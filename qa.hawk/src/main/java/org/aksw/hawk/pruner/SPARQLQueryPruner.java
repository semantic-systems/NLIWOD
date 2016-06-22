package org.aksw.hawk.pruner;

import java.util.HashSet;
import java.util.Set;

import javax.xml.ws.http.HTTPException;

import org.aksw.hawk.datastructures.HAWKQuestion;
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
	// TODO make this logging / JSON more pretty
	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queries, HAWKQuestion q) {

		Set<SPARQLQuery> returnedQueries = null;
		JSONArray document = new JSONArray();
		// Pruning
		JSONObject tmp = new JSONObject();
		tmp.put("label", "Number of Queries before pruning");
		tmp.put("value", queries.size());
		document.add(tmp);

		// this pruning should be first since it assures valid queries for the
		// following steps
		returnedQueries = underdefined.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "Underdefined pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = containsProjVariable.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries containing no project variable pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = containsTooManyNodesAsTextLookUp.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries containing too many nodes as text lookup pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = predicatesPerVariableEdge.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with more than one predicate between the same variables pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = numberOfTypesPerVariable.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with more than one type per variable pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = BGPisConnected.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries without connected BGP pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = cyclicTriple.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries containing cycic triple pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = hasBoundVariables.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries without bound variables pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = textFilterOverVariables.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries without text filter over existing variables pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = unboundTriple.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with unbound triples pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;

		returnedQueries = typemismatch.prune(queries, q);
		tmp = new JSONObject();
		tmp.put("label", "SPARQL queries with mismatching types pruned");
		tmp.put("value", (queries.size() - returnedQueries.size()));
		tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
		document.add(tmp);
		queries = returnedQueries;
		try {
			returnedQueries = disjointness.prune(queries, q);
			tmp = new JSONObject();
			tmp.put("label", "SPARQL queries with disjoint classes pruned");
			tmp.put("value", (queries.size() - returnedQueries.size()));
			tmp.put("removedQueries", queries2json(queriesDiff(queries, returnedQueries)));
			document.add(tmp);
			queries = returnedQueries;
		} catch (HTTPException e) {
			log.error("Cannot prune based on disjointness due to unavailable endpoint", e);
		}

		tmp = new JSONObject();
		tmp.put("label", "Number of Queries after really short pruning");
		tmp.put("value", queries.size());
		tmp.put("queries", queries2json(queries));
		document.add(tmp);

		q.setPruning_messages(document);
		log.debug(document.toJSONString());
		// TODO prune things like
		// ?const <http://dbpedia.org/ontology/deathDate> ?proj.
		// ?const <http://dbpedia.org/ontology/deathYear> ?proj.
		return queries;

	}

	private static Set<SPARQLQuery> queriesDiff(Set<SPARQLQuery> originalQueries, Set<SPARQLQuery> modifiedQueries) {
		Set<SPARQLQuery> result = new HashSet<>(originalQueries);
		result.removeAll(modifiedQueries);
		return result;
	}

	private static JSONArray queries2json(Set<SPARQLQuery> queries) {
		JSONArray jsonQueries = new JSONArray();

		for (SPARQLQuery query : queries) {
			JSONObject jsonQuery = new JSONObject();
			jsonQuery.put("query", query.toString()); // TODO: how to serialize
			                                          // the query?
			jsonQueries.add(jsonQuery);
		}

		return jsonQueries;
	}

}
