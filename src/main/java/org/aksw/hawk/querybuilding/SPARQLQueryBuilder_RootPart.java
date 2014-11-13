package org.aksw.hawk.querybuilding;

import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class SPARQLQueryBuilder_RootPart {

	private Logger log = LoggerFactory.getLogger(SPARQLQueryBuilder_RootPart.class);

	public Set<SPARQLQuery> buildRootPart(Set<SPARQLQuery> queryStrings, Question q) throws CloneNotSupportedException {
		Set<SPARQLQuery> sb = Sets.newHashSet();
		MutableTreeNode root = q.tree.getRoot();

		// full-text stuff e.g. "protected"
		if (!root.getAnnotations().isEmpty()) {
			for (SPARQLQuery query : queryStrings) {
				for (String anno : root.getAnnotations()) {
					// root has a valuable annotation from NN* or VB*
					SPARQLQuery variant1 = ((SPARQLQuery) query.clone());
					variant1.addConstraint("?proj  <" + anno + "> ?const.");

					SPARQLQuery variant2 = ((SPARQLQuery) query.clone());
					variant2.addConstraint("?const <" + anno + "> ?proj.");

					// root has annotations but they are not valuable, e.g.
					// took, is, was, ride
					SPARQLQuery variant3 = ((SPARQLQuery) query.clone());
					variant3.addConstraint("?const  ?p ?proj.");

					SPARQLQuery variant4 = ((SPARQLQuery) query.clone());
					variant4.addConstraint("?proj   ?p ?const.");

					sb.add(variant1);
					sb.add(variant2);
					sb.add(variant3);
					sb.add(variant4);

					// FIXME is this correct
					SPARQLQuery variant5 = ((SPARQLQuery) query.clone());
					variant5.addFilterOverAbstractsContraint("?proj", root.label);
					sb.add(variant5);

				}
			}
		} else {
			// TODO do the full text stuff
			sb.addAll(queryStrings);
		}
		return sb;
	}
}