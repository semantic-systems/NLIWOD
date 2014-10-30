package org.aksw.hawk.pruner;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class GraphNonSCCPruner {
	static Logger log = LoggerFactory.getLogger(GraphNonSCCPruner.class);

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings) {
		Set<SPARQLQuery> returnList = Sets.newHashSet();
		for (SPARQLQuery query : queryStrings) {
			// build graph
			Graph g = new Graph(query);
			// look whether each node is reachable from each other
			if (g.isSCC()) {
				returnList.add(query);
			}
		}
		return returnList;
	}

	public boolean isSCC(SPARQLQuery query) {
		// build graph
		Graph g = new Graph(query);
		// look whether each node is reachable from each other
		if (g.isSCC()) {
			return true;
		} else {
			return false;
		}
	}

	public static void main(String args[]) {
		// FIXME transform me to a UNIT test
		// List<SPARQLQuery> queries = Lists.newArrayList();
		// String queryString =
		// "SELECT ?a0 WHERE {?a0 a <http://dbpedia.org/ontology/City>. ?a1 <http://dbpedia.org/ontology/birthPlace> ?a0. }";
		// queries.add(new ParameterizedSparqlString(queryString));
		// queryString =
		// "SELECT ?a0 WHERE {?a0 a <http://dbpedia.org/ontology/City>. ?a0 <http://dbpedia.org/ontology/birthPlace> ?a3. }";
		// queries.add(new ParameterizedSparqlString(queryString));
		// queryString =
		// "SELECT ?a0 WHERE {?a0 a <http://dbpedia.org/ontology/City>. ?a2 <http://dbpedia.org/ontology/deathPlace> ?a1. }";
		// queries.add(new ParameterizedSparqlString(queryString));
		//
		// GraphNonSCCPruner gSCCPruner = new GraphNonSCCPruner();
		// log.debug("" + queries.size());
		// queries = gSCCPruner.prune(queries);
		// log.debug("" + queries.size());
	}

	private class Graph {
		int nodeCount = 0;
		Map<String, Integer> mapStringInt = Maps.newHashMap();
		boolean[][] edgeMatrix = null;

		public Graph(SPARQLQuery query) {
			for (String triple : query.constraintTriples) {
				String s = triple.split(" ")[0];
				String o = triple.split(" ")[2];
				if (!mapStringInt.containsKey(s)) {
					mapStringInt.put(s, nodeCount);
					nodeCount++;
				}
				if (!mapStringInt.containsKey(o)) {
					mapStringInt.put(o, nodeCount);
					nodeCount++;
				}
			}
			edgeMatrix = new boolean[nodeCount][];
			for (int i = 0; i < nodeCount; i++) {
				edgeMatrix[i] = new boolean[nodeCount];
			}
			for (String triple : query.constraintTriples) {
				String s = triple.split(" ")[0];
				String o = triple.split(" ")[2];
				int ss = mapStringInt.get(s);
				int oo = mapStringInt.get(o);
				edgeMatrix[ss][oo] = true;
				edgeMatrix[oo][ss] = true;
			}
		}

		public boolean isSCC() {
			boolean[] visited = new boolean[nodeCount];
			Stack<Integer> stack = new Stack<Integer>();
			stack.push(0);
			while (!stack.isEmpty()) {
				Integer currentNode = stack.pop();
				visited[currentNode] = true;
				for (int i = 0; i < nodeCount; i++) {
					if (edgeMatrix[currentNode][i] && !visited[i]) {
						stack.push(i);
					}
				}
			}
			for (int i = 0; i < nodeCount; i++) {
				if (!visited[i]) {
					return false;
				}
			}
			return true;
		}
	}

}
