package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPNode;
import com.hp.hpl.jena.graph.impl.LiteralLabelFactory;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class Module {
	public List<ParameterizedSparqlString> statementList = new ArrayList<>();
	Logger log = LoggerFactory.getLogger(getClass());

	public Module(DEPNode rootPredicate, DEPNode node, Question q) {
		buildRDFTypeStatement(rootPredicate, node);
		buildPredicateStatement(rootPredicate, node);

	}

	private void buildPredicateStatement(DEPNode rootPredicate, DEPNode node) {
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		pss.appendNode(new ResourceImpl());
		pss.appendIri(rootPredicate.form);

		if (node.form.startsWith("http://")) {
			pss.appendNode(new ResourceImpl(node.form));
		} else {
			pss.append(LiteralLabelFactory.create(node.form, "en"));
		}

		log.debug("\t\t\t" + pss.getCommandText());
		statementList.add(pss);
	}

	private void buildRDFTypeStatement(DEPNode rootPredicate, DEPNode node) {
		ParameterizedSparqlString pss = new ParameterizedSparqlString();
		pss.appendNode(new ResourceImpl());
		pss.appendIri("rdf:type");
		if (rootPredicate.form.startsWith("http://")) {
			pss.appendNode(new ResourceImpl(rootPredicate.form));
		} else {
			pss.append(LiteralLabelFactory.create(rootPredicate.form, "en"));
		}
		log.debug("\t\t\t" + pss.getCommandText());
		statementList.add(pss);
	}

}
