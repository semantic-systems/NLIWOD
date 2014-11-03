package org.aksw.hawk.querybuilding;

import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.index.DBAbstractsIndex;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SPARQLQueryBuilder {
	Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder.class);
	SPARQLQueryBuilder_ProjectionPart projection;
	SPARQLQueryBuilder_RootPart root;
	SPARQL sparql = new SPARQL();

	public SPARQLQueryBuilder(DBAbstractsIndex index) {
		projection = new SPARQLQueryBuilder_ProjectionPart();
		root = new SPARQLQueryBuilder_RootPart(index);
	}

	public Map<String, Set<RDFNode>> build(Question q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();
		try {
			// build projection part
			Set<SPARQLQuery> queryStrings = projection.buildProjectionPart(this, q);
			queryStrings = root.buildRootPart(queryStrings, q);
			queryStrings = buildConstraintPart(queryStrings, q);
			int i = 0;
			for (SPARQLQuery query : queryStrings) {
				if (queryHasBoundVariables(query)) {
					log.info(i++ + "/" + queryStrings.size() + "= " + query.toString().substring(0, Math.min(1000, query.toString().length())));
					Set<RDFNode> answerSet = sparql.sparql(query);
					if (!answerSet.isEmpty()) {
						answer.put(query.toString(), answerSet);
					}
				}
			}
		} catch (CloneNotSupportedException e) {
			log.error(e.getLocalizedMessage(), e);
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		} finally {
			System.gc();
		}
		return answer;
	}

	private boolean queryHasBoundVariables(SPARQLQuery queryString) {
		for (String triple : queryString.constraintTriples) {
			if (triple.contains("http")) {
				return true;
			}
		}
		if (queryString.filter.isEmpty()) {
			return false;
		}
		return false;
	}

	private Set<SPARQLQuery> buildConstraintPart(Set<SPARQLQuery> queryStrings, Question q) throws CloneNotSupportedException {
		Set<SPARQLQuery> sb = Sets.newHashSet();
		// TODO only valid for questions with one constraint node
		if (q.tree.getRoot().getChildren().size() == 2) {
			MutableTreeNode tmp = q.tree.getRoot().getChildren().get(1);
			while (tmp != null) {
				log.info("Current node: " + tmp);
				if (tmp.posTag.equals("ADD")) {
					for (SPARQLQuery query : queryStrings) {
						// GIVEN ?proj ?root ?const or ?const ?root ?proj
						if (query.constraintsContains("?const")) {
							SPARQLQuery variant1 = (SPARQLQuery) query.clone();
							variant1.addConstraint("?proj ?pbridge <" + tmp.label + ">.");
							sb.add(variant1);
							SPARQLQuery variant2 = (SPARQLQuery) query.clone();
							variant2.addFilter("const", Lists.newArrayList(tmp.label));
							sb.add(variant2);
						}
						// GIVEN no constraint yet given and root incapable for those purposes
						else {
							SPARQLQuery variant2 = (SPARQLQuery) query.clone();
							variant2.addConstraint("?proj ?pbridge <" + tmp.label + ">.");
							sb.add(variant2);
						}
					}
				} else if (tmp.posTag.equals("CombinedNN")) {
					for (SPARQLQuery query : queryStrings) {
						if (!tmp.getAnnotations().isEmpty()) {
							SPARQLQuery variant1 = (SPARQLQuery) query.clone();
							variant1.addFilter("proj", tmp.getAnnotations());
							sb.add(variant1);

							SPARQLQuery variant2 = (SPARQLQuery) query.clone();
							variant2.addFilter("const", tmp.getAnnotations());
							sb.add(variant2);
						}
					}
				} else if (tmp.posTag.equals("NN")) {
					for (SPARQLQuery query : queryStrings) {
						if (!tmp.getAnnotations().isEmpty()) {
							for (String annotation : tmp.getAnnotations()) {
								SPARQLQuery variant1 = (SPARQLQuery) query.clone();
								variant1.addConstraint("?proj a <" + annotation + ">.");
								sb.add(variant1);
								SPARQLQuery variant2 = (SPARQLQuery) query.clone();
								variant2.addConstraint("?const a <" + annotation + ">.");
								sb.add(variant2);
							}
						}
					}
				} else if (tmp.posTag.equals("VBD")) {
					// TODO refuse
				} else {
					log.error("unhandled path");
				}
				if (!tmp.getChildren().isEmpty()) {
					if (tmp.getChildren().size() > 0) {
						tmp = tmp.getChildren().get(0);
					} else {
						log.error("More children in constraint part than expected");
					}
				} else {
					tmp = null;
				}
			}
		} else {
			log.error("more children than expected");
			// TODO go on here
			// sb.addAll(queryStrings);
		}
		return sb;

	}
}
