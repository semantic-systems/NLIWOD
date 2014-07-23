package org.aksw.hawk.pruner;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

public class GraphNonSCCPruner {
	static Logger log = LoggerFactory.getLogger(GraphNonSCCPruner.class);

	public List<ParameterizedSparqlString> prune(List<ParameterizedSparqlString> queries) {
		List<ParameterizedSparqlString> returnList = Lists.newArrayList();
		for (ParameterizedSparqlString query : queries) {
			// build graph
			Graph g = new Graph(query);
			// look whether each node is reachable from each other
			if (g.isSCC()) {
				returnList.add(query);
			}
		}
		return returnList;
	}

	public boolean isSCC(ParameterizedSparqlString query) {
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
		List<ParameterizedSparqlString> queries = Lists.newArrayList();
		String queryString = "SELECT ?a0 WHERE {?a0 a <http://dbpedia.org/ontology/City>. ?a1 <http://dbpedia.org/ontology/birthPlace> ?a0. }";
		queries.add(new ParameterizedSparqlString(queryString));
		queryString = "SELECT ?a0 WHERE {?a0 a <http://dbpedia.org/ontology/City>. ?a0 <http://dbpedia.org/ontology/birthPlace> ?a3. }";
		queries.add(new ParameterizedSparqlString(queryString));
		queryString = "SELECT ?a0 WHERE {?a0 a <http://dbpedia.org/ontology/City>. ?a2 <http://dbpedia.org/ontology/deathPlace> ?a1. }";
		queries.add(new ParameterizedSparqlString(queryString));

		GraphNonSCCPruner gSCCPruner = new GraphNonSCCPruner();
		log.debug("" + queries.size());
		queries = gSCCPruner.prune(queries);
		log.debug("" + queries.size());
	}

	private class Graph {
		int nodeCount = 0;
		Map<Node, Integer> mapStringInt = Maps.newHashMap();
		boolean[][] edgeMatrix = null;

		public Graph(ParameterizedSparqlString query) {
			List<Element> elements = ((ElementGroup) query.asQuery().getQueryPattern()).getElements();
			for (Element elem : elements) {
				if (elem instanceof ElementPathBlock) {
					ElementPathBlock pathBlock = (ElementPathBlock) elem;
					for (TriplePath triple : pathBlock.getPattern().getList()) {
						Node s = triple.getSubject();
						Node o = triple.getObject();
						if (!mapStringInt.containsKey(s)) {
							mapStringInt.put(s, nodeCount);
							nodeCount++;
						}
						if (!mapStringInt.containsKey(o)) {
							mapStringInt.put(o, nodeCount);
							nodeCount++;
						}
					}
				}
			}
			edgeMatrix = new boolean[nodeCount][];
			for (int i = 0; i < nodeCount; i++) {
				edgeMatrix[i] = new boolean[nodeCount];
			}
			for (Element elem : elements) {
				if (elem instanceof ElementPathBlock) {
					ElementPathBlock pathBlock = (ElementPathBlock) elem;
					for (TriplePath triple : pathBlock.getPattern().getList()) {
						Node s = triple.getSubject();
						Node o = triple.getObject();
						int ss = mapStringInt.get(s);
						int oo = mapStringInt.get(o);
						edgeMatrix[ss][oo] = true;
						edgeMatrix[oo][ss] = true;
					}
				}
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
