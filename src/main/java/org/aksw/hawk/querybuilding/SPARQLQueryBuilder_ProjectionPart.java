package org.aksw.hawk.querybuilding;

import java.util.List;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SPARQLQueryBuilder_ProjectionPart {

	private Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder_ProjectionPart.class);

	Set<SPARQLQuery> buildProjectionPart(SPARQLQueryBuilder sparqlQueryBuilder, Question q) {
		Set<SPARQLQuery> queries = Sets.newHashSet();
		List<MutableTreeNode> bottomUp = getProjectionPathBottumUp(q);
		// empty restriction for projection part in order to account for misinformation in left tree
		// TODO this leads to tremendous increase of runtime
		// queries.add(new StringBuilder("?proj ?p ?o."));
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
							for (String annotation : bottom.getAnnotations()) {
								// TODO probably here is a bug
								queries.add(new SPARQLQuery("?proj a <" + annotation + ">."));
								// TODO add super class,e.g., City -> Settlement
							}
							// TODO cities like http://dbpedia.org/page/Kirzhach are not annotated as db-owl:Place
							queries.add(new SPARQLQuery("?proj a ?type."));
							// need to add this due to Kirzhach which is in DBpedia 3.9 not typed, not even as db-owl:Place
							// so we leaf out the urgent generation of a type information
							queries.add(new SPARQLQuery());
						} else {
							log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
						}
					} else {
						// is either from Where or Who
						if (bottom.getAnnotations().size() > 0) {
							for (String annotation : bottom.getAnnotations()) {
								for (SPARQLQuery existingQuery : queries) {
									existingQuery.addConstraint("?proj a <" + annotation + ">.");
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
							SPARQLQuery queryString = new SPARQLQuery("?proj ?p ?o.");
							queryString.addFilterOverAbstractsContraint("?proj", bottom.label, queryString);
							queries.add(queryString);
							queryString = new SPARQLQuery("?proj ?p ?o.");
							queryString.addFilterOverAbstractsContraint("?o", bottom.label, queryString);
							queries.add(queryString);
							// IMPORTANT if tree has not the projection variable in the left most path the projection variable could be on the right side and thus in case of not inverse properties we need to turn around this logic
							queryString = new SPARQLQuery("?o ?p ?proj.");
							queryString.addFilterOverAbstractsContraint("?o", bottom.label, queryString);
							queries.add(queryString);
							queryString = new SPARQLQuery("?o ?p ?proj.");
							queryString.addFilterOverAbstractsContraint("?proj", bottom.label, queryString);
							queries.add(queryString);
						} else {
							log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
						}
					} else {
						// combined nouns are lists of abstracts containing does words, i.e., type constraints
						if (bottom.getAnnotations().size() > 0) {
							for (SPARQLQuery existingQueries : queries) {
								existingQueries.addConstraint("?proj ?p ?o.");
								existingQueries.addFilterOverAbstractsContraint("?proj", bottom.label, existingQueries);
							}
						} else {
							log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
						}
					}
				} else {
					// strange case since entities should not be the question word type
					sparqlQueryBuilder.log.error("Strange case that never should happen: " + bottomposTag);
				}

			} else {
				// TODO build it in a way, that says that down here are only projection variable constraining modules that need to be advanced by the top heuristically say that here NNs or VBs stand
				// for a predicates
				if (bottomposTag.equals("CombinedNN") && topPosTag.matches("VB(.)*|NN(.)*")) {
					for (String predicates : top.getAnnotations()) {
						if (bottom.getAnnotations().size() > 0) {
							SPARQLQuery queryString = new SPARQLQuery("?proj <" + predicates + "> ?o.");
							queryString.addFilterOverAbstractsContraint("?proj", bottom.label, queryString);
							queries.add(queryString);

							queryString = new SPARQLQuery("?o <" + predicates + "> ?proj.");
							queryString.addFilterOverAbstractsContraint("?proj", bottom.label, queryString);
							queries.add(queryString);
						}
					}
					i++;
				} else if (bottomposTag.matches("ADD|NN") && topPosTag.matches("VB(.)*|NN(.)*|CombinedNN")) {
					// either way it is an unprecise verb binding
					if (!topPosTag.matches("CombinedNN")) {
						for (String annotation : top.getAnnotations()) {
							SPARQLQuery queryString = new SPARQLQuery("?proj <" + annotation + "> <" + bottom.label + ">.");
							queries.add(queryString);
						}
					}
					// or it stems from a full-text look up (+ reversing of the predicates)
					if (top.getAnnotations().size() > 0) {
						SPARQLQuery queryString = new SPARQLQuery("?proj ?p <" + bottom.label + ">.");
						queryString.addFilterOverAbstractsContraint("?proj", top.label, queryString);
						queries.add(queryString);

						queryString = new SPARQLQuery("<" + bottom.label + "> ?p ?proj.");
						queryString.addFilterOverAbstractsContraint("?proj", top.label, queryString);
						queries.add(queryString);
					}
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