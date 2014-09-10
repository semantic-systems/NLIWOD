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
			if (q.languageToQuestion.get("en").contains("philosopher")) {
				System.out.println();
			}
			// build projection part
			Set<SPARQLQuery> queryStrings = projection.buildProjectionPart(this, q);
			queryStrings = root.buildRootPart(queryStrings, q);
			queryStrings = buildConstraintPart(queryStrings, q);
			int i = 0;
			for (SPARQLQuery queryString : queryStrings) {
				String query = queryString.toString();
				log.debug(i++ + "/" + queryStrings.size() + "= " + query.substring(0, Math.min(1000, query.length())));
				Set<RDFNode> answerSet = sparql.sparql(query);
				if (!answerSet.isEmpty()) {
					answer.put(query, answerSet);
				}
			}
		} catch (CloneNotSupportedException e) {
			log.error(e.getLocalizedMessage(), e);
		} finally {
			System.gc();
		}
		return answer;
	}

	private Set<SPARQLQuery> buildConstraintPart(Set<SPARQLQuery> queryStrings, Question q) throws CloneNotSupportedException {
		Set<SPARQLQuery> sb = Sets.newHashSet();
		// TODO only valid for questions with one constraint node
		if (q.tree.getRoot().getChildren().size() == 2 && q.tree.getRoot().getChildren().get(1).getChildren().isEmpty()) {
			log.info(q.tree.toString());
			MutableTreeNode tmp = q.tree.getRoot().getChildren().get(1);

			if (tmp.posTag.equals("ADD")) {
				for (SPARQLQuery query : queryStrings) {
					// GIVEN ?proj ?root ?const or ?const ?root ?proj
					// TODO ??? && !tmp.getAnnotations().isEmpty()
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
			} else {
				log.error("unhandled path");
				// TODO go on here

				// sb.addAll(queryStrings);
			}
		} else {
			log.error("more children than expected");
			// TODO go on here
			// sb.addAll(queryStrings);
		}
		return sb;

	}

}
