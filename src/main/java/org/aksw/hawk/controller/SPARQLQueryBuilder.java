package org.aksw.hawk.controller;

import java.util.Set;

import javax.xml.ws.http.HTTPException;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.util.stack.Stack;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class SPARQLQueryBuilder {

	Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder.class);

	public Set<Set<RDFNode>> build(Question q) {
		Set<Set<RDFNode>> answer = Sets.newHashSet();

		StringBuilder queryString = new StringBuilder("SELECT ?proj WHERE {\n");

		buildProjectionPart(queryString, q);

		queryString.append("}");

		System.out.println(queryString.toString());
		answer.add(sparql(queryString.toString()));
		return answer;
	}

	private void buildProjectionPart(StringBuilder queryString, Question q) {
		Stack<MutableTreeNode> stack = new Stack<>();
		stack.push(q.tree.getRoot().getChildren().get(0));
		// iterate through left tree part
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			String label = tmp.label;
			String posTag = tmp.posTag;

			// only one projection variable node
			if (tmp.children.size() == 0) {
				if (posTag.matches("WRB|WP")) {
					// is either from Where or Who
					if (tmp.getAnnotations().size() == 1) {
						queryString.append("?proj a <" + tmp.getAnnotations().get(0) + ">.");
					} else {
						log.error("Too many or too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
					}
				} else if (posTag.equals("CombinedNN")) {
					// combined nouns are lists of abstracts containing does
					// words, i.e., type constraints
					if (tmp.getAnnotations().size() >0) {
						queryString.append("?proj ?p ?o.");
						queryString.append("FILTER (?proj IN (");
						for (ResourceImpl annotation : tmp.getAnnotations()) {
							queryString.append("<" + annotation.getURI() + "> , ");
						}
						queryString.deleteCharAt(queryString.lastIndexOf(","));
						queryString.append(")).");
					} else {
						log.error("Too many or too less annotations for projection part of the tree!", q.languageToQuestion.get("en"));
					}

				} else if (posTag.matches("NN(.)*")) {
					// DBO look up
				} else {
					// strange case
					// since entities should not be the question word type
					log.error("Strange case that never should happen: " + posTag);
				}
			}
		}
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
