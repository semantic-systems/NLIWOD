package org.aksw.hawk.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.index.DBAbstractsIndex;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class SPARQLQueryBuilder {
	Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder.class);
	SPARQLQueryBuilder_ProjectionPart projection = new SPARQLQueryBuilder_ProjectionPart();
	SPARQL sparql = new SPARQL();
	DBAbstractsIndex index = new DBAbstractsIndex();

	public Map<String, Set<RDFNode>> build(Question q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();
		// build projection part
		Set<StringBuilder> queryStrings = projection.buildProjectionPart(this, q);
		queryStrings = buildConstraintPart(queryStrings, q);
		for (StringBuilder queryString : queryStrings) {
			String query = "SELECT ?proj WHERE {\n " + queryString.toString() + "}";
			Set<RDFNode> answerSet = sparql.sparql(query);
			if (!answerSet.isEmpty()) {
				answer.put(queryString.toString(), answerSet);
			}
		}
		return answer;
	}

	private Set<StringBuilder> buildConstraintPart(Set<StringBuilder> queryStrings, Question q) {
		Set<StringBuilder> sb = Sets.newHashSet();
		MutableTreeNode root = q.tree.getRoot();

		{ // full-text stuff like protected
			//TODO bug here, this gets added to early to all other things, must be handled like variant 5 below
			for (StringBuilder query : queryStrings) {
				List<String> uris = index.listAbstractsContaining(root.label);
				StringBuilder fulltextConstraint = new StringBuilder("FILTER (?proj IN (\n");
				for (String annotation : uris) {
					fulltextConstraint.append("<" + annotation + "> , ");
				}
				fulltextConstraint.deleteCharAt(fulltextConstraint.lastIndexOf(",")).append(")).");
				sb.add(query.append(fulltextConstraint.toString()));
			}
		}
		if (!root.getAnnotations().isEmpty()) {
			for (StringBuilder query : queryStrings) {
				for (ResourceImpl anno : root.getAnnotations()) {
					// root has a valuable annotation from NN* or VB*
					StringBuilder variant1 = new StringBuilder(query.toString()).append("?proj  <" + anno + "> ?const.");
					StringBuilder variant2 = new StringBuilder(query.toString()).append("?const <" + anno + "> ?proj.");
					// root has annotations but they are not valuable, e.g. took, is, was, ride
					StringBuilder variant3 = new StringBuilder(query.toString()).append("?const  ?p ?proj.");
					StringBuilder variant4 = new StringBuilder(query.toString()).append("?proj   ?p ?const.");

					sb.add(variant1);
					sb.add(variant2);
					sb.add(variant3);
					sb.add(variant4);
					// TODO build other variants

				}
			}
		} else {
			// TODO do the full text stuff
			sb.addAll(queryStrings);
		}
		return sb;
	}

}
