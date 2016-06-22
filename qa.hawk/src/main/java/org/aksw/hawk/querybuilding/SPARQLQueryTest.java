package org.aksw.hawk.querybuilding;

import java.util.Set;

import org.apache.jena.rdf.model.RDFNode;
import org.junit.Test;

public class SPARQLQueryTest {

	@Test
	public void test() {
		SPARQL sqb = new SPARQL();
		SPARQLQuery query = new SPARQLQuery();
		query.addConstraint("?proj a <http://dbpedia.org/ontology/Cleric>.");
		query.addConstraint("?proj ?p ?const.");
		System.out.println("Test");
		// query.addFilterOverAbstractsContraint("?proj","http://dbpedia.org/resource/Pope_John_Paul_I,
		// http://dbpedia.org/resource/Pope_John_Paul_II");
		query.addFilterOverAbstractsContraint("?proj", "\'http://dbpedia.org/resource/Pope_John_Paul_I\'");

		// query.addFilter("'?proj',
		// ('http://dbpedia.org/resource/Pope_John_Paul_I','http://dbpedia.org/resource/Pope_John_Paul_II')");

		query.addFilterOverAbstractsContraint("?const", "Wadowice");
		// TODO Christian: Doesn't accept apostrophes yet. J.Paul II's
		// birthplace is therefore unreachable.
		for (String q : query.generateQueries()) {
			Set<RDFNode> set = sqb.sparql(q);
			for (RDFNode item : set) {
				System.out.println(item);
			}
		}
	}
}
