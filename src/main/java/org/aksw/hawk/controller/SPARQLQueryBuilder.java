package org.aksw.hawk.controller;

import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class SPARQLQueryBuilder {
	Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder.class);
	SPARQLQueryBuilder_ProjectionPart projection = new SPARQLQueryBuilder_ProjectionPart(new SPARQL());

	public Map<String, Set<RDFNode>> build(Question q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();
		// build projection part
		if (q.languageToQuestion.get("en").contains("crown")) {
			System.out.println();
		}
		Set<StringBuilder> queryStrings = projection.buildProjectionPart(this, q);
		queryStrings = constrain(queryStrings, q);
		for (StringBuilder queryString : queryStrings) {
			String query = "SELECT ?proj WHERE {\n " + queryString.toString() + "}";
			Set<RDFNode> answerSet = projection.sparql.sparql(query);
			if (!answerSet.isEmpty()) {
				answer.put(queryString.toString(), answerSet);
			}
		}
		return answer;
	}

	private Set<StringBuilder> constrain(Set<StringBuilder> queryStrings, Question q) {
		Set<StringBuilder> sb = Sets.newHashSet();
		MutableTreeNode root = q.tree.getRoot();
		if (!root.getAnnotations().isEmpty()) {
			for (StringBuilder query : queryStrings) {
				for (ResourceImpl anno : root.getAnnotations()) {
					StringBuilder variant1 = new StringBuilder(query.toString()).append("?proj <" + anno + "> ?const.");
					StringBuilder variant2 = new StringBuilder(query.toString()).append("?const <" + anno + "> ?proj.");
					//TODO build other variants
					sb.add(variant1);
					sb.add(variant2);
				}
			}
		} else {
			// TODO do the full text stuff
			sb.addAll(queryStrings);
		}
		return sb;
	}

}
