package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;

import com.hp.hpl.jena.query.ParameterizedSparqlString;

public class PseudoQueryBuilder {

	public List<ParameterizedSparqlString> buildQuery(Question q) {

		List<ParameterizedSparqlString> queries = new ArrayList<ParameterizedSparqlString>();

		// TODO choose projection variable wisely

		boolean[] print = new boolean[q.modules.size()];
		boolean finished = false;
		while (!finished) {
			ParameterizedSparqlString query = new ParameterizedSparqlString();
			query.setCommandText("SELECT ?a WHERE {}");

			finished = true;
			for (int i = 0; i < print.length; i++) {
				if (print[i]) {
					query.append(q.modules.get(i).statementList.get(0));
				} else {
					query.append(q.modules.get(i).statementList.get(1));
					finished = false;
				}
			}
			addOneBinary(print);
			queries.add(query);
		}

		return queries;
	}

	private void addOneBinary(boolean[] print) {
		boolean carry = true;
		int i = 0;
		while (carry && i < print.length) {
			if (print[i]) {
				print[i] = false;
				carry = true;
				i++;
			} else {
				print[i] = true;
				carry = false;
			}
		}

	}
}
