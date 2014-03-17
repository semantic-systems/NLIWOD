package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;

import com.clearnlp.dependency.DEPNode;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class Module {
	public List<ParameterizedSparqlString> statementList = new ArrayList<>();

	public Module(DEPNode rootPredicate, DEPNode node, Question q) {
		List<Entity> entities = q.languageToNamedEntites.get("en");
		buildRDFTypeStatement(rootPredicate, node);
		buildPredicateStatement(rootPredicate, node);

	}

	private void buildPredicateStatement(DEPNode rootPredicate, DEPNode node) {
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		pss.appendNode(new ResourceImpl());
		pss.clearParam(0);
		pss.appendIri(rootPredicate.form);
		pss.appendNode(new ResourceImpl());
		pss.clearParam(3);

		System.out.println(pss.toString());
		statementList.add(pss);
	}

	private void buildRDFTypeStatement(DEPNode rootPredicate, DEPNode node) {
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		pss.appendNode(new ResourceImpl());
		pss.clearParam(0);
		pss.appendIri("rdf:type");
		pss.appendIri(rootPredicate.form);
		System.out.println(pss.toString());

	}

}
