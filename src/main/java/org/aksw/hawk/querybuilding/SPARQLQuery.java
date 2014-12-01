package org.aksw.hawk.querybuilding;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SPARQLQuery implements Cloneable {

	private static HashSet<String> stopwords = Sets.newHashSet("of", "and");
	public Set<String> constraintTriples = Sets.newHashSet();
	public Set<String> filter = Sets.newHashSet();
	public Map<String, Set<String>> textMapFromVariableToSetOfFullTextToken = Maps.newHashMap();

	public SPARQLQuery(String initialConstraint) {
		constraintTriples.add(initialConstraint);
	}

	/**
	 * only for clone()
	 */
	protected SPARQLQuery() {
	}

	public void addConstraint(String constraint) {
		constraintTriples.add(constraint);
	}

	public void addFilterOverAbstractsContraint(String variable, String label) {
		// ?s text:query (<http://dbpedia.org/ontology/abstract> 'Mandela
		// anti-apartheid activist').

		String[] separatedLabel = label.split("[ \\-]");
		// to search in a string with whitespaces like "Nobel Prize"
		if (textMapFromVariableToSetOfFullTextToken.containsKey(variable)) {
			Set<String> set = textMapFromVariableToSetOfFullTextToken.get(variable);
			for (String item : separatedLabel) {
				set.add(item);
			}
			textMapFromVariableToSetOfFullTextToken.put(variable, set);
		} else {
			textMapFromVariableToSetOfFullTextToken.put(variable, Sets.newHashSet(separatedLabel));
		}
	}

	public boolean constraintsContains(String target) {
		for (String c : constraintTriples) {
			if (c.contains(target)) {
				return true;
			}
		}
		return false;
	}

	public void addFilter(String string) {
		filter.add(string);

	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		SPARQLQuery q = new SPARQLQuery();
		q.constraintTriples = Sets.newHashSet();
		for (String constraint : this.constraintTriples) {
			q.constraintTriples.add(constraint);
		}
		q.filter = Sets.newHashSet();
		for (String key : this.filter) {
			q.filter.add(key);
		}
		q.textMapFromVariableToSetOfFullTextToken = Maps.newHashMap();
		for (String key : this.textMapFromVariableToSetOfFullTextToken.keySet()) {
			Set<String> list = Sets.newHashSet(this.textMapFromVariableToSetOfFullTextToken.get(key));
			q.textMapFromVariableToSetOfFullTextToken.put(key, list);
		}
		return q;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PREFIX text:    <http://jena.apache.org/text#> \n");
		sb.append("SELECT DISTINCT ?proj WHERE {\n ");
		for (String variable : textMapFromVariableToSetOfFullTextToken.keySet()) {
			// ?s text:query (<http://dbpedia.org/ontology/abstract> 'Mandela
			// anti-apartheid activist').
			sb.append(variable + " text:query (<http://dbpedia.org/ontology/abstract> '");
			ArrayList<String> list = Lists.newArrayList(textMapFromVariableToSetOfFullTextToken.get(variable));
			for (int i = 0; i < list.size(); i++) {
				// Stopwords introduced to prevent Lucene from doing quatsch
				if (!stopwords.contains(list.get(i))) {
					// TODO photographer does not match photographers in index
					// temporary solution is a a hack with ~ for fuzzy
					sb.append(list.get(i) + "~1");
					if (i + 1 < list.size()) {
						sb.append(" AND ");
					}
				}
			}
			// return 100 uris from text index
			sb.append("' 1000). \n");
		}
		for (String constraint : constraintTriples) {
			sb.append(constraint + " \n");
		}
		for (String filterString : filter) {
			sb.append("FILTER (" + filterString + ").\n ");
		}
		sb.append("}\n");
		// FIXME quick fix for reducing processing time assuming result set is
		// smaller than 10
		sb.append("LIMIT 12");
		return sb.toString();
	}
}
