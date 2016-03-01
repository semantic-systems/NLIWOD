package org.aksw.hawk.pruner;

import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class BGPisConnected implements ISPARQLQueryPruner {
	static Logger log = LoggerFactory.getLogger(BGPisConnected.class);

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, HAWKQuestion q) {
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

	private class Graph {
		int nodeCount = 0;
		Map<String, Integer> mapStringInt = Maps.newHashMap();
		boolean[][] edgeMatrix = null;

		public Graph(SPARQLQuery query) {
			String split[];
			for (String fusekiVariable : query.textMapFromVariableToSingleFuzzyToken.keySet()) {
				// ?proj text:query (<http://dbpedia.org/ontology/abstract>
				// 'influenced~1').
				if (!mapStringInt.containsKey(fusekiVariable)) {
					mapStringInt.put(fusekiVariable, nodeCount);
					nodeCount++;
				}
			}
			for (String triple : query.constraintTriples) {
				triple = triple.replaceAll("\\s+", " ");
				split = triple.split(" ");
				String s = split[0];
				String o = split[2];
				if (o.endsWith(".")) {
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
				triple = triple.replaceAll("\\s+", " ");
				split = triple.split(" ");
				String s = split[0];
				String o = split[2];
				if (o.endsWith(".")) {
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
