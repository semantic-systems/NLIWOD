package org.aksw.hawk.module;

import java.io.File;
import java.net.URL;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.RDF;

public class SimpleModule extends Module {

	private Model model;

	public SimpleModule() {
		super();
		this.model = ModelFactory.createDefaultModel();
		FileManager.get().readModel(model, (new File("resources/dbpedia_3.9.owl")).getAbsolutePath());
	}

	public void addStatement(String subject, String predicate, String object) {
		// TODO if label which can be URI is not promising use string label
		WhereClause wc = null;
		if (predicate.equals(RDF.type.getURI())) {
			String dboTerm = dboIndex.search(object);
			if (dboTerm != null) {
				wc = new WhereClause(subject, predicate, dboTerm);
				String superClass = superClassOf(dboTerm);
				if (superClass != null && !superClass.equals(dboTerm)) {
					statementList.add(wc);
					wc = new WhereClause(subject, predicate, superClass);
				}
			} else {
				wc = new WhereClause(subject, predicate, object);
			}
		} else if (predicate.equals("IS")) {
			// skip since subject is var and object is a dbpedia resource or noun
			wc = new WhereClause(subject, predicate, object);
		} else {
			String dboTerm = dboIndex.search(predicate);
			if (dboTerm != null) {
				wc = new WhereClause(subject, dboTerm, object);
			} else {
				wc = new WhereClause(subject, predicate, object);
			}
		}
		statementList.add(wc);

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (WhereClause x : statementList) {
			sb.append("\t" + x.s + " " + x.p + " " + x.o + "\n");
		}
		return sb.toString();
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

		QueryExecution qexec = QueryExecutionFactory.create(sparqlQuery, model);
		ResultSet results = qexec.execSelect();
		while (results.hasNext()) {
			// TODO improve returning first best result
			return results.next().get("?o").asResource().getURI();
		}
		qexec.close();
		return dboTerm;
	}

}
