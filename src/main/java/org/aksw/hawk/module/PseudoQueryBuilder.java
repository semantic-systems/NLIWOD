package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ParameterizedSparqlString;

public class PseudoQueryBuilder {
	Logger log = LoggerFactory.getLogger(PseudoQueryBuilder.class);

	public List<ParameterizedSparqlString> buildQuery(Question q) {
		List<ParameterizedSparqlString> queries = new ArrayList<ParameterizedSparqlString>();
		if (q.modules == null) {
			return null;
		}
		int numberOfModules = q.modules.size();
		// init print with each number of statements per module
		int[] print = new int[numberOfModules];
		for (int i = 0; i < print.length; i++) {
			print[i] = q.modules.get(i).statementList.size() - 1;
		}
		// iterate until all permutations are reached
		boolean finished = false;
		while (!finished) {
			finished = true;
			StringBuilder queryString = buildQuery(q, print);
			finished = minusOne(print, q);
			if (queryString != null) {
				log.debug("Query: " + queryString);
				// think of print as a map which shows the current permutations
				// minus one generates the next permutation using clock paradigm

				ParameterizedSparqlString query = new ParameterizedSparqlString(queryString.toString());
				queries.add(query);
			}
		}

		log.debug("Number of queries: " + queries.size());
		return queries;
	}

	private StringBuilder buildQuery(Question q, int[] print) {
		StringBuilder queryString = new StringBuilder("SELECT ?a0 WHERE {\n");
		Set<String> vars = getVars(q, print);
		for (int i = 0; i < print.length; i++) {
			Module currentModule = q.modules.get(i);
			WhereClause currentChoiceOfStatement = currentModule.statementList.get(print[i]);
			// replacement rule gets activated
			if (currentChoiceOfStatement.p.equals("IS")) {
				// if variable in s is not in the set of variables discard query
				if (vars.contains(currentChoiceOfStatement.s)) {
					String s = "\\" + currentChoiceOfStatement.s;
					String o = currentChoiceOfStatement.o;
					String querySoFar = queryString.toString();
					queryString = new StringBuilder(querySoFar.replaceAll(s, o));
				} else {
					return null;
				}
			} else {
				queryString.append(currentChoiceOfStatement.toString());
				queryString.append("\n");
			}
		}
		queryString.append("}");
		return queryString;
	}

	private Set<String> getVars(Question q, int[] print) {
		Set<String> vars = Sets.newHashSet();
		for (int i = 0; i < print.length; i++) {
			Module currentModule = q.modules.get(i);
			WhereClause currentStatment = currentModule.statementList.get(print[i]);
			if (currentStatment.s.startsWith("?") && !currentStatment.p.equals("IS")) {
				vars.add(currentStatment.s);
			}
			if (currentStatment.o.startsWith("?") && !currentStatment.p.equals("IS")) {
				vars.add(currentStatment.o);
			}
		}
		return vars;
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
}
