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
		if (q.languageToQuestion.get("en").contains("crown")) {
			System.out.println();
		}
		Set<StringBuilder> queryStrings = buildProjectionPart(q);
		for (StringBuilder queryString : queryStrings) {
			String query = "SELECT ?proj WHERE {\n " + queryString.toString() + "}";
			Set<RDFNode> answerSet = sparql.sparql(query);
			if (!answerSet.isEmpty()) {
				answer.put(queryString.toString(), answerSet);
			}
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
					if (queries.isEmpty()) {
						// is either from Where or Who
						if (bottom.getAnnotations().size() > 0) {
							for (ResourceImpl annotation : bottom.getAnnotations()) {
								queries.add(new StringBuilder("?proj a <" + annotation + ">."));
								// TODO add super class,e.g., City -> Settlement
							}
						} else {
							log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
						}
					} else {
						// is either from Where or Who
						if (bottom.getAnnotations().size() > 0) {
							for (ResourceImpl annotation : bottom.getAnnotations()) {
								for (StringBuilder existingQueries : queries) {
									existingQueries.append("?proj a <" + annotation + ">.");
									// TODO add super class,e.g., City -> Settlement
								}
							}
						} else {
							log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
						}
					}
				} else if (bottomposTag.equals("CombinedNN")) {
					if (queries.isEmpty()) {
						// combined nouns are lists of abstracts containing does words, i.e., type constraints
						if (bottom.getAnnotations().size() > 0) {
							StringBuilder queryString = new StringBuilder("?proj ?p ?o.\nFILTER (?proj IN (\n");
							joinURIsForFilterExpression(bottom, queryString);
							queries.add(queryString);
						} else {
							log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
						}
					} else {
						// combined nouns are lists of abstracts containing does words, i.e., type constraints
						if (bottom.getAnnotations().size() > 0) {
							for (StringBuilder existingQueries : queries) {
								existingQueries.append("?proj ?p ?o.\nFILTER (?proj IN (\n");
								joinURIsForFilterExpression(bottom, existingQueries);
							}
						} else {
							log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
						}
					}
				} else {
					// strange case since entities should not be the question word type
					log.error("Strange case that never should happen: " + bottomposTag);
				}

			} else {
				// TODO build it in a way, that says that down here are only projection variable constraining modules that need to be advanced by the top heuristically say that here NNs or VBs stand
				// for a predicates
				if (bottomposTag.equals("CombinedNN") && topPosTag.matches("VB(.)*|NN(.)*")) {
					for (ResourceImpl predicates : top.getAnnotations()) {
						StringBuilder queryString = new StringBuilder("?proj <" + predicates + "> ?o.\nFILTER (?proj IN (\n");
						joinURIsForFilterExpression(bottom, queryString);
						queries.add(queryString);

						queryString = new StringBuilder("?o <" + predicates + "> ?proj.\nFILTER (?proj IN (\n");
						joinURIsForFilterExpression(bottom, queryString);
						queries.add(queryString);
					}
					i++;
				} else if (bottomposTag.matches("ADD|NN") && topPosTag.matches("VB(.)*|NN(.)*|CombinedNN")) {
					// either way it is an unprecise verb binding
					if (!topPosTag.matches("CombinedNN")) {
						for (ResourceImpl annotation : top.getAnnotations()) {
							StringBuilder queryString = new StringBuilder("?proj <" + annotation + "> <" + bottom.label + ">.");
							queries.add(queryString);
						}
					}
					// or it stems from a full-text look up (+ reversing of the predicates)
					StringBuilder queryString = new StringBuilder("?proj ?p <" + bottom.label + ">.\nFILTER (?proj IN (\n");
					joinURIsForFilterExpression(top, queryString);
					queries.add(queryString);

					queryString = new StringBuilder("<" + bottom.label + "> ?p ?proj.\nFILTER (?proj IN (\n");
					joinURIsForFilterExpression(top, queryString);
					queries.add(queryString);
					i++;
				} else {
					log.error("Strange case that never should happen: " + bottomposTag);
				}
			}
		}
		return queries;
	}

	/**
	 * 
	 * @param top
	 *            contains |URIs|=n
	 * @param queryString
	 *            contains so far a SOMETHING. FILTER (?proj IN (...)). Goal is to insert the URIs into the brackets in a valid SPARQL way
	 */
	private void joinURIsForFilterExpression(MutableTreeNode top, StringBuilder queryString) {
		for (ResourceImpl annotation : top.getAnnotations()) {
			queryString.append("<" + annotation.getURI() + "> , ");
		}
		queryString.deleteCharAt(queryString.lastIndexOf(",")).append(")).");
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
