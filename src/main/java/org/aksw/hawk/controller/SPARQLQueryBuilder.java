package org.aksw.hawk.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class SPARQLQueryBuilder {
	Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder.class);
	SPARQL sparql = new SPARQL();

	public Map<String, Set<RDFNode>> build(Question q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();
		// build projection part
		Set<StringBuilder> queryStrings = buildProjectionPart(q);
		for (StringBuilder queryString : queryStrings) {
			answer.put(queryString.toString(), sparql.sparql(queryString.toString()));
		}
		return answer;
	}

	private Set<StringBuilder> buildProjectionPart(Question q) {
		Set<StringBuilder> queries = Sets.newHashSet();
		List<MutableTreeNode> bottomUp = getProjectionPathBottumUp(q);
		for (int i = 0; i < bottomUp.size(); ++i) {
			MutableTreeNode bottom = bottomUp.get(i);
			String bottomposTag = bottom.posTag;
			MutableTreeNode top = bottom.parent;
			String topPosTag = bottom.parent.posTag;
			// head of this node is root element
			if (top.parent == null) {
				if (bottomposTag.matches("WRB|WP|NN(.)*")) {
					// is either from Where or Who
					if (bottom.getAnnotations().size() > 0) {
						for (ResourceImpl annotation : bottom.getAnnotations()) {
							StringBuilder queryString = new StringBuilder("SELECT ?proj WHERE {\n");
							queryString.append("?proj a <" + annotation + ">.\n}");
							queries.add(queryString);
							// TODO add super class,e.g., City -> Settlement
						}
					} else {
						log.error("Too many or too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
					}
				} else if (bottomposTag.equals("CombinedNN")) {
					// combined nouns are lists of abstracts containing does
					// words, i.e., type constraints
					if (bottom.getAnnotations().size() > 0) {
						StringBuilder queryString = new StringBuilder("SELECT ?proj WHERE {\n");
						queryString.append("?proj ?p ?o.\n").append("FILTER (?proj IN (\n");
						for (ResourceImpl annotation : bottom.getAnnotations()) {
							queryString.append("<" + annotation.getURI() + "> , ");
						}
						queryString.deleteCharAt(queryString.lastIndexOf(",")).append(")).}");
						queries.add(queryString);
					} else {
						log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
					}
				} else {
					// strange case since entities should not be the question
					// word type
					log.error("Strange case that never should happen: " + bottomposTag);
				}
			} else {
				// TODO build it in a way, that says that down here are only
				// projection variable constraining modules that need to be
				// advanced by the top
				// heuristically say that here NNs or VBs stand for a predicates
				if (bottomposTag.equals("CombinedNN") && topPosTag.matches("VB(.)*|NN(.)*")) {
					for (ResourceImpl predicates : top.getAnnotations()) {
						StringBuilder queryString = new StringBuilder("SELECT ?proj WHERE {\n");
						queryString.append("?proj <" + predicates + "> ?o.\n").append("FILTER (?proj IN (\n");
						for (ResourceImpl annotation : bottom.getAnnotations()) {
							queryString.append("<" + annotation.getURI() + "> , ");
						}
						queryString.deleteCharAt(queryString.lastIndexOf(",")).append(")).}");
						queries.add(queryString);
						queryString = new StringBuilder("SELECT ?proj WHERE {\n");
						queryString.append("?o <" + predicates + "> ?proj.\n").append("FILTER (?proj IN (\n");
						for (ResourceImpl annotation : bottom.getAnnotations()) {
							queryString.append("<" + annotation.getURI() + "> , ");
						}
						queryString.deleteCharAt(queryString.lastIndexOf(",")).append(")).}");
						queries.add(queryString);
					}
					i++;
				} else if (bottomposTag.equals("ADD") && topPosTag.matches("VB(.)*|NN(.)*")) {
					// either way it is an unprecise verb binding
					for (ResourceImpl annotation : top.getAnnotations()) {
						StringBuilder queryString = new StringBuilder("SELECT ?proj WHERE {\n");
						queryString.append("?proj <" + annotation + "> <" + bottom.label + ">.\n}");
						queries.add(queryString);
					}
					// or it stems from a full-text look up (+ reversing of the
					// predicates)
					StringBuilder queryString = new StringBuilder("SELECT ?proj WHERE {\n");
					queryString.append("?proj ?p <" + bottom.label + ">.\n").append("FILTER (?proj IN (\n");
					for (ResourceImpl annotation : top.getAnnotations()) {
						queryString.append("<" + annotation.getURI() + "> , ");
					}
					queryString.deleteCharAt(queryString.lastIndexOf(",")).append(")).}");
					queries.add(queryString);
					queryString = new StringBuilder("SELECT ?proj WHERE {\n");
					queryString.append("<" + bottom.label + "> ?p ?proj.\n").append("FILTER (?proj IN (\n");
					for (ResourceImpl annotation : top.getAnnotations()) {
						queryString.append("<" + annotation.getURI() + "> , ");
					}
					queryString.deleteCharAt(queryString.lastIndexOf(",")).append(")).}");
					queries.add(queryString);
					i++;
				} else {
					log.error("Strange case that never should happen: " + bottomposTag);
				}
			}
		}
		return queries;
	}

	private List<MutableTreeNode> getProjectionPathBottumUp(Question q) {
		List<MutableTreeNode> bottomUp = Lists.newArrayList();
		// iterate through left tree part
		// assumption: this part of the tree is a path
		MutableTreeNode tmp = q.tree.getRoot().getChildren().get(0);
		while (tmp != null) {
			bottomUp.add(tmp);
			if (!tmp.getChildren().isEmpty()) {
				tmp = tmp.getChildren().get(0);
			} else {
				tmp = null;
			}
		}
		bottomUp = Lists.reverse(bottomUp);
		return bottomUp;
	}
}
