package org.aksw.hawk.module;

import java.util.Iterator;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ParameterizedSparqlString;

public class PseudoQueryBuilder {
	private static final String IS = "<IS>";
	Logger log = LoggerFactory.getLogger(PseudoQueryBuilder.class);
	private int[] print;
	private Question question;

	public Iterator<ParameterizedSparqlString> buildQuery(Question q) {
		this.question = q;
		HAWKIterator iter = new HAWKIterator();
		if (q.modules == null) {
			return null;
		}
		int numberOfModules = q.modules.size();
		// init print with each number of statements per module
		print = new int[numberOfModules];
		for (int i = 0; i < print.length; i++) {
			print[i] = q.modules.get(i).statementList.size() - 1;
		}

		return iter;
	}

	private class HAWKIterator implements Iterator<ParameterizedSparqlString> {

		boolean finished = false;

		@Override
		public boolean hasNext() {
			return !finished;
		}

		@Override
		public ParameterizedSparqlString next() {
			// iterate until all permutations are reached
			StringBuilder queryString = buildQueryString();
			finished = minusOne();
			if (queryString == null) {
				// query could not be build
				return null;
			}
			log.debug("Query: " + queryString);
			// think of print as a map which shows the current permutations
			// minus one generates the next permutation using clock paradigm

			ParameterizedSparqlString query = new ParameterizedSparqlString(queryString.toString());

			return query;

		}

		@Override
		public void remove() {
			throw new RuntimeException("Remove is not supported");
		}

	}

	private StringBuilder buildQueryString() {
		StringBuilder queryString = new StringBuilder("SELECT ?a0 WHERE {\n");
		Set<String> vars = getVars();
		for (int i = 0; i < print.length; i++) {
			Module currentModule = question.modules.get(i);
			WhereClause currentChoiceOfStatement = currentModule.statementList.get(print[i]);
			// replacement rule gets activated
			if (currentChoiceOfStatement.p.equals(IS)) {
				// if variable in s is not in the set of variables discard query
				// TODO error here
				if (vars.contains(currentChoiceOfStatement.s)) {
					String s = "\\" + currentChoiceOfStatement.s;
					String o = currentChoiceOfStatement.o;
					String querySoFar = queryString.toString();
					queryString = new StringBuilder(querySoFar.replaceAll(s, o));
				} else {
					// subject variable in triple of ?s IS <URL>. is not in
					// setOfVars, return null to decrease print [] and generate
					// new query
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

	private Set<String> getVars() {
		Set<String> vars = Sets.newHashSet();
		for (int i = 0; i < print.length; i++) {
			Module currentModule = question.modules.get(i);
			WhereClause currentStatment = currentModule.statementList.get(print[i]);
			if (currentStatment.s.startsWith("?") && !currentStatment.p.equals(IS)) {
				vars.add(currentStatment.s);
			}
			if (currentStatment.o.startsWith("?") && !currentStatment.p.equals(IS)) {
				vars.add(currentStatment.o);
			}
		}
		return vars;
	}

	private boolean minusOne() {
		int pointer = print.length - 1;
		while (pointer >= 0) {
			if (print[pointer] > 0) {
				print[pointer]--;
				return false;
			} else {
				// if we have no chance to decrease the current position
				// we set it to its initial value and decrease afterwards the
				// next larger position
				print[pointer] = question.modules.get(pointer).statementList.size() - 1;
				pointer--;
			}
		}
		return true;
	}
}
