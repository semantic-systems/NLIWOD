package org.aksw.hawk.module;

import org.aksw.hawk.nlp.posTree.MutableTreeNode;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.vocabulary.RDF;

public class ProjectionModule extends Module {

	public ProjectionModule(MutableTreeNode type) {
		String dboTerm = dboIndex.search(type.label);
		log.debug("DBOTerm: " + dboTerm);

		WhereClause wc = new WhereClause();
		wc.s = "?uri";
		wc.p = RDF.type.asResource().getURI();
		wc.o = type.label;
		statementList.add(wc);

		if (dboTerm != null) {
			wc = new WhereClause();
			wc.s = "?uri";
			wc.p = RDF.type.asResource().getURI();
			wc.o = dboTerm;
			statementList.add(wc);

			wc = new WhereClause();
			wc.s = "?uri";
			wc.p = RDF.type.asResource().getURI();
			wc.o = superClassOf(dboTerm);
			statementList.add(wc);
		}
	}

	private String superClassOf(String dboTerm) {
		// TODO hack nur großgeschriebene sachen verwenden um classes statt
		// objectproperties zu verwenden
		String before = dboTerm.substring(0, dboTerm.lastIndexOf("/") + 1);
		String lowerCase = dboTerm.substring(dboTerm.lastIndexOf("/") + 1, dboTerm.lastIndexOf("/") + 2);
		String after = dboTerm.substring(dboTerm.lastIndexOf("/") + 2, dboTerm.length());
		lowerCase = lowerCase.toUpperCase();
		dboTerm = before + lowerCase + after;
		String q = "select distinct ?o where { <" + dboTerm + "> <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?o.}";
		Query sparqlQuery = QueryFactory.create(q);
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", sparqlQuery);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				// TODO improve returning first best result
				return results.next().get("?o").asResource().getURI();
			}
		} finally {
			qexec.close();
			log.error("Run into error");
		}
		return dboTerm;
	}
}
