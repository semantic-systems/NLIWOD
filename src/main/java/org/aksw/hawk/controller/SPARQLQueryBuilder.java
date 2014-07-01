package org.aksw.hawk.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.http.HTTPException;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.util.stack.Stack;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class SPARQLQueryBuilder {

	Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder.class);

	public Map<String, Set<RDFNode>> build(Question q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();

		List<StringBuilder> queryStrings = buildProjectionPart(q);
		for (StringBuilder queryString : queryStrings) {
			answer.put(queryString.toString(), sparql(queryString.toString()));
		}
		return answer;
	}

	private List<StringBuilder> buildProjectionPart(Question q) {
		List<StringBuilder> queries = Lists.newArrayList();

		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(q.tree.getRoot().getChildren().get(0));
		// iterate through left tree part
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			String posTag = tmp.posTag;

			// only one projection variable node
			if (tmp.children.size() == 0) {
				if (posTag.matches("WRB|WP")) {
					// is either from Where or Who
					if (tmp.getAnnotations().size() == 1) {
						StringBuilder queryString = new StringBuilder("SELECT ?proj WHERE {\n");
						queryString.append("?proj a <" + tmp.getAnnotations().get(0) + ">.\n");
						queryString.append("}");
						queries.add(queryString);
					} else {
						log.error("Too many or too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
					}
				} else if (posTag.equals("CombinedNN")) {
					// combined nouns are lists of abstracts containing does
					// words, i.e., type constraints
					if (tmp.getAnnotations().size() > 0) {
						StringBuilder queryString = new StringBuilder("SELECT ?proj WHERE {\n");
						queryString.append("?proj ?p ?o.\n");
						queryString.append("FILTER (?proj IN (\n");
						for (ResourceImpl annotation : tmp.getAnnotations()) {
							queryString.append("<" + annotation.getURI() + "> , ");
						}
						queryString.deleteCharAt(queryString.lastIndexOf(","));
						queryString.append(")).");
						queryString.append("}");
						queries.add(queryString);
					} else {
						log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
					}
				} else if (posTag.matches("NN(.)*")) {
					// projection node is either a property or a class
					if (tmp.getAnnotations().size() > 0) {
						for (ResourceImpl annotation : tmp.getAnnotations()) {
							StringBuilder queryString = new StringBuilder("SELECT ?proj WHERE {\n");
							queryString.append("?proj a <" + annotation + ">.\n");
							queryString.append("}");
							queries.add(queryString);
						}
					} else {
						log.error("Too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
					}
				} else {
					// strange case
					// since entities should not be the question word type
					log.error("Strange case that never should happen: " + posTag);
				}
			}
		}
		return queries;
	}

	private Set<RDFNode> sparql(String query) {
		Set<RDFNode> set = Sets.newHashSet();
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				set.add(results.next().get("?proj"));
			}
		} catch (HTTPException e) {
			log.error("Query: " + query, e);
		} finally {
			qexec.close();
		}
		return set;
	}

}
