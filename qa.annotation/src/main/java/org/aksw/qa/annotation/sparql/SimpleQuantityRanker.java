package org.aksw.qa.annotation.sparql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.qa.commons.sparql.SPARQL;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.rdf.model.RDFNode;

public class SimpleQuantityRanker {
	private SPARQL sparql = new SPARQL();
	private final static String DBPEDIA_ONTO_URI = "http://dbpedia.org/ontology/";

	/**
	 * For property, counts how many relations with this property exist. The one
	 * with the most relations is returned. For classes, counts how many
	 * subtypes of this class are existing. the one with the most subtypes is
	 * returned. If properties and classes are among the uris, the one with the
	 * biggest count is returned.
	 *
	 * @param uris
	 * @return
	 */
	public String rank(final Collection<String> uris) {

		Map<Integer, String> uriToQuantity = new HashMap<>();

		for (String it : uris) {
			ArrayList<RDFNode> answers = new ArrayList<>(sparql.sparql(constructQuery(it)));
			System.out.println(constructQuery(it));
			RDFNode node = answers.get(0);
			uriToQuantity.put(((Integer) node.asLiteral().getValue()), it);
		}

		List<Integer> sorted = new ArrayList<>(uriToQuantity.keySet());

		Collections.sort(sorted);

		return uriToQuantity.get(Iterables.getLast(sorted));
	}

	private String constructQuery(final String uri) {

		Character character = uri.charAt(DBPEDIA_ONTO_URI.length());

		if (Character.isLowerCase(character)) {
			// lowercase start so property in dbpedia
			return "SELECT(count(*) AS ?proj)  {?x <" + uri + "> ?y . }";
		} else {
			// uppercase so class in dbpedia
			return "SELECT (count(*) AS ?proj) {?x a  <" + uri + "> .  }";
		}
	}

}
