package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;

import com.hp.hpl.jena.query.ParameterizedSparqlString;

public class PseudoQueryBuilder {

	public List<ParameterizedSparqlString> buildQuery(Question q) {
		List<ParameterizedSparqlString> queries = new ArrayList<ParameterizedSparqlString>();
		int numberOfModules = q.modules.size();
		boolean[] print = new boolean[numberOfModules];
		boolean finished = false;
		while (!finished) {
			ParameterizedSparqlString query = new ParameterizedSparqlString();
			buildCommandText(query, q);
			finished = true;

			for (int i = 0; i < print.length; i++) {
				// TODO build a query more wisely
				if (print[i]) {
					replaceParameters(query, q.modules.get(i).statementList.get(0), i);
				} else {
					replaceParameters(query, q.modules.get(i).statementList.get(1), i);
					finished = false;
				}
			}

			addOneBinary(print);
			queries.add(query);
		}
		return queries;
	}

	private void replaceParameters(ParameterizedSparqlString query, WhereClause whereClause, int parameterNumber) {
		query.setIri("xp" + parameterNumber, whereClause.p);
		if (whereClause.o.startsWith("http://")) {
			query.setIri("xo" + parameterNumber, whereClause.o);

		} else {
			query.setLiteral("xo" + parameterNumber, whereClause.o);
		}

	}

	private void buildCommandText(ParameterizedSparqlString query, Question q) {
		// TODO choose projection variable wisely
		String tmp = "SELECT ?xs0 WHERE {\n";
		for (int i = 0; i < q.modules.size(); i++) {
			// subject
			tmp += "?xs" + i + " ";
			// predicate
			tmp += "?xp" + i + " ";
			// object
			tmp += "?xo" + i + ".\n";
		}
		tmp += "}";
		query.setCommandText(tmp);
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
