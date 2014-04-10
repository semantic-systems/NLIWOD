package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.apache.xerces.impl.xs.opti.NodeImpl;
import org.openrdf.query.algebra.Var;

import com.google.common.base.Joiner;
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
		return queries;
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
			query.setParam("xs" + parameterNumber, new Node_Variable(s.replace("?", "")));
		}
		// keep bgp forming variable
		if (s.equals("?xo1")) {
			query.setParam("xs" + parameterNumber, new Node_Variable(s.replace("?", "")));
		}

		// set predicate
		query.setIri("xp" + parameterNumber, p);

		// handle object
		if (o.startsWith("http://")) {
			query.setIri("xo" + parameterNumber, o);
		} else if (o.startsWith("?")) {
			query.setParam("xo" + parameterNumber, new Node_Variable(o.replace("?", "")));
		} else {
			query.setLiteral("xo" + parameterNumber, o);
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

}
