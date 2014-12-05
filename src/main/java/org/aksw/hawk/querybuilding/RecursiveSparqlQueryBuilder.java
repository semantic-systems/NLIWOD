package org.aksw.hawk.querybuilding;

import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class RecursiveSparqlQueryBuilder {
	Logger log = LoggerFactory.getLogger(RecursiveSparqlQueryBuilder.class);

	public Set<SPARQLQuery> start(SPARQLQueryBuilder sparqlQueryBuilder, Question q) {
		Set<SPARQLQuery> returnSet = Sets.newHashSet(new SPARQLQuery());
		Set<String> variableSet = Sets.newHashSet("?proj", "?const");
		try {
			MutableTreeNode tmp = q.tree.getRoot();

			recursion(returnSet, variableSet, tmp);

		} catch (CloneNotSupportedException e) {
			log.error("Exception while recursion", e);
		}

		return returnSet;
	}

	private void recursion(Set<SPARQLQuery> returnSet, Set<String> variableSet, MutableTreeNode tmp) throws CloneNotSupportedException {
		Set<SPARQLQuery> sb = Sets.newHashSet();

		// if no annotations maybe a CombinedNN
		if (!tmp.getAnnotations().isEmpty()) {
			for (SPARQLQuery query : returnSet) {
				for (String anno : tmp.getAnnotations()) {
					if (tmp.posTag.matches("VB(.)*")) {
						// FIXME variablen iterieren
						SPARQLQuery variant1 = ((SPARQLQuery) query.clone());
						variant1.addConstraint("?proj  <" + anno + "> ?const.");

						SPARQLQuery variant2 = ((SPARQLQuery) query.clone());
						variant2.addConstraint("?const <" + anno + "> ?proj.");

						sb.add(variant1);
						sb.add(variant2);
					} else if (tmp.posTag.matches("NN(.)*")) {
						SPARQLQuery variant3 = ((SPARQLQuery) query.clone());
						variant3.addConstraint("?const a <" + anno + ">.");

						SPARQLQuery variant4 = ((SPARQLQuery) query.clone());
						variant4.addConstraint("?proj a <" + anno + ">.");

						SPARQLQuery variant5 = ((SPARQLQuery) query.clone());
						
						sb.add(variant3);
						sb.add(variant4);
						sb.add(variant5);
					} else {
						log.error("Tmp: " + tmp.label + " pos: " + tmp.posTag);
					}
				}
			}
		} else {
			if (tmp.posTag.equals("CombinedNN")) {
				for (SPARQLQuery query : returnSet) {
					SPARQLQuery variant1 = (SPARQLQuery) query.clone();
					variant1.addFilterOverAbstractsContraint("?proj", tmp.label);

					SPARQLQuery variant2 = (SPARQLQuery) query.clone();
					variant2.addFilterOverAbstractsContraint("?const", tmp.label);

					sb.add(variant1);
					sb.add(variant2);
				}
			} else if (tmp.posTag.matches("VB(.)*")) {
				for (SPARQLQuery query : returnSet) {
					SPARQLQuery variant1 = (SPARQLQuery) query.clone();
					variant1.addFilterOverAbstractsContraint("?proj", tmp.label);

					SPARQLQuery variant2 = (SPARQLQuery) query.clone();
					variant2.addFilterOverAbstractsContraint("?const", tmp.label);

					sb.add(variant1);
					sb.add(variant2);
				}
			} else if (tmp.posTag.matches("ADD")) {
				for (SPARQLQuery query : returnSet) {
					SPARQLQuery variant1 = (SPARQLQuery) query.clone();
					variant1.addConstraint("?proj ?pbridge <" + tmp.label + ">.");
					
					SPARQLQuery variant2 = (SPARQLQuery) query.clone();
					variant2.addFilter("?proj IN (<" + tmp.label + ">)");
					
					SPARQLQuery variant3 = (SPARQLQuery) query.clone();

					sb.add(variant1);
					sb.add(variant2);
					sb.add(variant3);
				}
			} else {
				log.error("Tmp: " + tmp.label + " pos: " + tmp.posTag);
			}
		}
		returnSet.clear();
		returnSet.addAll(sb);

		for (MutableTreeNode child : tmp.getChildren()) {
			recursion(returnSet, variableSet, child);
		}

	}
}
