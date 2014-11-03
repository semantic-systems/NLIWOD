package org.aksw.hawk.querybuilding;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class SPARQLQuery implements Cloneable {

	public Set<String> constraintTriples =  Sets.newHashSet();
	public Set<String> filter = Sets.newHashSet();
	public Map<String, Set<String>> filterBifContains = Maps.newHashMap();

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

	public void addFilterOverAbstractsContraint(String variable, String label, SPARQLQuery q) {
		q.addConstraint(variable + " <http://dbpedia.org/ontology/abstract> ?abstract" + variable.replace("?", "") + ".");
		String[] whitespaceSeparatedLabel = label.split(" ");
		// to search in a string with whitespaces like "Nobel Prize"
		if (whitespaceSeparatedLabel.length > 1) {
			label = "'" + label + "'";
		}
		if (filterBifContains.containsKey(variable)) {
			Set<String> set = filterBifContains.get(variable);
			set.add(label);
			filterBifContains.put(variable, set);
		} else {
			filterBifContains.put(variable, Sets.newHashSet(label));
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
		for(String constraint: this.constraintTriples){
			q.constraintTriples.add(constraint);
		}
		q.filter = Sets.newHashSet();
		for (String key : this.filter) {
			q.filter.add(key);
		}
		q.filterBifContains = Maps.newHashMap();
		for (String key : this.filterBifContains.keySet()) {
			Set<String> list = Sets.newHashSet(this.filterBifContains.get(key));
			q.filterBifContains.put(key, list);
		}
		return q;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT DISTINCT ?proj WHERE {\n ");
		for (String constraint : constraintTriples) {
			sb.append(constraint + " \n");
		}
		for (String filterString : filter) {
			sb.append("FILTER (" + filterString + ").\n ");
		}
		for (String variable : filterBifContains.keySet()) {
			sb.append("FILTER (");
			sb.append("<bif:contains>(?abstract" + variable.replace("?", "") + ",\"");
			ArrayList<String> list = Lists.newArrayList(filterBifContains.get(variable));
			for (int i = 0; i < list.size(); i++) {
				sb.append(list.get(i));
				if (i + 1 < list.size()) {
					sb.append(" and ");
				}
			}
			sb.append("\")).\n");
		}
		sb.append("}\n");
		// FIXME quick fix for reducing processing time assuming result set is
		// smaller than 10
		sb.append("LIMIT 1000");
		return sb.toString();
	}
}
