package org.aksw.hawk.pruner;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ParameterizedSparqlString;

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
		Set<SPARQLQuery> queries = Sets.newHashSet();
		SPARQLQuery query = new SPARQLQuery("?proj a <http://dbpedia.org/ontology/Writer>.");
		query.addConstraint("?const a <http://dbpedia.org/ontology/Philosopher>. ");
		query.addConstraint("?const <http://dbpedia.org/ontology/influencedBy> ?proj. ");
		query.addConstraint("?const <http://dbpedia.org/ontology/abstract> ?abstractconst. ");
		query.addFilterOverAbstractsContraint("?const", "Nobel Prize");
		query.addFilterOverAbstractsContraint("?const", "refused");
		System.out.println(query);
		queries.add(query);
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

		GraphNonSCCPruner gSCCPruner = new GraphNonSCCPruner();
		System.out.println("" + queries.size());
		queries = gSCCPruner.prune(queries);
		System.out.println("" + queries.size());

	}

	private class Graph {
		int nodeCount = 0;
		Map<String, Integer> mapStringInt = Maps.newHashMap();
		boolean[][] edgeMatrix = null;

		public Graph(SPARQLQuery query) {
			String split[];
			for (String triple : query.constraintTriples) {
				split = triple.split(" ");
				String s = split[0];
				String o = split[2];
				if(o.endsWith(".")) {
					o = o.substring(0, o.length() - 1);
				}
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
				split = triple.split(" ");
				String s = split[0];
				String o = split[2];
				if(o.endsWith(".")) {
					o = o.substring(0, o.length() - 1);
				}
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
