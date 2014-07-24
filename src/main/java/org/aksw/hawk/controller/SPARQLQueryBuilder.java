package org.aksw.hawk.controller;

import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class SPARQLQueryBuilder {
	Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder.class);
	SPARQLQueryBuilder_ProjectionPart data = new SPARQLQueryBuilder_ProjectionPart(new SPARQL());

	public Map<String, Set<RDFNode>> build(Question q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();
		// build projection part
		if (q.languageToQuestion.get("en").contains("crown")) {
			System.out.println();
		}
		Set<StringBuilder> queryStrings = data.buildProjectionPart(this, q);
		for (StringBuilder queryString : queryStrings) {
			String query = "SELECT ?proj WHERE {\n " + queryString.toString() + "}";
			Set<RDFNode> answerSet = data.sparql.sparql(query);
			if (!answerSet.isEmpty()) {
				answer.put(queryString.toString(), answerSet);
			}
		}
		return answer;
	}

	
}
