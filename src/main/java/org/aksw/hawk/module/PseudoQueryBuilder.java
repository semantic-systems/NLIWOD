package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.query.ParameterizedSparqlString;

public class PseudoQueryBuilder {

	public List<ParameterizedSparqlString> buildQuery(Question q) {
		List<ParameterizedSparqlString> queries = new ArrayList<ParameterizedSparqlString>();
		int numberOfModules = q.modules.size();
		// init print with each number of statements per module
		int[] print = new int[numberOfModules];
		for (int i = 0; i < print.length; i++) {
			print[i] = q.modules.get(i).statementList.size() - 1;
		}
		// iterate until all permutations are reached
		boolean finished = false;
		while (!finished) {
			ParameterizedSparqlString query = new ParameterizedSparqlString();
			buildCommandText(query, q);
			finished = true;
			for (int i = 0; i < print.length; i++) {
				Module currentModule = q.modules.get(i);
				WhereClause currentChoiceOfStatement = currentModule.statementList.get(print[i]);
				replaceParameters(query, currentChoiceOfStatement, i);
			}
			// think of print as a map which shows the current permutations
			// minus one will generate the next permutation using a clock
			// paradigm
			finished = minusOne(print, q);
			queries.add(query);
		}

		queries = rebuildQueriesWithCorrectParameters(queries);
		return queries;
	}

	private List<ParameterizedSparqlString> rebuildQueriesWithCorrectParameters(List<ParameterizedSparqlString> queries) {
		List<ParameterizedSparqlString> tmpList = Lists.newArrayList();
		for (ParameterizedSparqlString q : queries) {
			String queryString = q.asQuery().toString();
			ParameterizedSparqlString tmpQuery = new ParameterizedSparqlString(queryString);
			tmpList.add(tmpQuery);
		}

		return tmpList;
	}

	private boolean minusOne(int[] print, Question q) {
		int pointer = print.length - 1;
		while (pointer >= 0) {
			if (print[pointer] > 0) {
				print[pointer]--;
				return false;
			} else {
				// if we have no chance to decrease the current position
				// we set it to its initial value and decrease afterwards the
				// next larger position
				print[pointer] = q.modules.get(pointer).statementList.size() - 1;
				pointer--;
			}
		}

		return true;
	}

	private void replaceParameters(ParameterizedSparqlString query, WhereClause whereClause, int parameterNumber) {
		String s = whereClause.s;
		String p = whereClause.p;
		String o = whereClause.o;
		// keep projection variable
		if (s.equals("?uri")) {
			query.setParam("xS" + parameterNumber, new Node_Variable(s.replace("?", "")));
		}
		// keep bgp forming variable
		if (s.equals("?xo1")) {
			query.setParam("xS" + parameterNumber, new Node_Variable(s.replace("?", "")));
		}
		// keep projection variable
		if (o.equals("?uri")) {
			query.setParam("xO" + parameterNumber, new Node_Variable(o.replace("?", "")));
		}
		// keep bgp forming variable
		if (o.equals("?xo1")) {
			query.setParam("xO" + parameterNumber, new Node_Variable(o.replace("?", "")));
		}

		// set predicate
		query.setIri("xP" + parameterNumber, p);

		// handle object
		if (o.startsWith("http://")) {
			query.setIri("xO" + parameterNumber, o);
		} else if (o.startsWith("?")) {
			query.setParam("xO" + parameterNumber, new Node_Variable(o.replace("?", "")));
		} else {
			query.setLiteral("xO" + parameterNumber, o);
		}

	}

	private void buildCommandText(ParameterizedSparqlString query, Question q) {
		String tmp = "SELECT ?uri WHERE {\n";
		for (int i = 0; i < q.modules.size(); i++) {
			// subject
			tmp += "?xS" + i + " ";
			// predicate
			tmp += "?xP" + i + " ";
			// object
			tmp += "?xO" + i + ".\n";
		}
		tmp += "}";
		query.setCommandText(tmp);
	}

}
