package org.aksw.hawk.module;

import org.aksw.hawk.index.DBOIndex;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;

import com.hp.hpl.jena.vocabulary.RDF;

public class ProjectionModule extends Module {

	public ProjectionModule(MutableTreeNode type) {
		DBOIndex dboIndex =  new DBOIndex();
		String dboTerm = dboIndex.search(  type.label);
		log.debug("DBOTerm: " + dboTerm);

		WhereClause wc = new WhereClause();
		wc.s = "?uri";
		wc.p = RDF.type.asResource().getURI();
		wc.o = type.label;
		statementList.add(wc);

		wc = new WhereClause();
		wc.s = "?uri";
		wc.p = RDF.type.asResource().getURI();
		wc.o = dboTerm;
		statementList.add(wc);
	}
}
