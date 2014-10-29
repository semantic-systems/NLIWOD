package org.aksw.hawk.querybuilding;

import java.util.ArrayList;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class SPARQLQuery implements Cloneable {

	ArrayList<String> constraintTriples = Lists.newArrayList();
	// list of FILTER( ?proj IN (<uri1>,...,<urin>)).
	// HashMap<String, List<String>> filter = Maps.newHashMap();
	Set<String> filter = Sets.newHashSet();

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

	// public void addFilter(String projectionVariable, List<String>
	// annotations) {
	// if (filter.containsKey(projectionVariable)) {
	// List<String> existingAnnotations = filter.get(projectionVariable);
	// existingAnnotations.retainAll(annotations);
	// filter.put(projectionVariable, existingAnnotations);
	// } else {
	// filter.put(projectionVariable, Lists.newArrayList(annotations));
	// }
	// }

	@SuppressWarnings("unchecked")
	@Override
	protected Object clone() throws CloneNotSupportedException {
		SPARQLQuery q = new SPARQLQuery();
		q.constraintTriples = (ArrayList<String>) this.constraintTriples.clone();
		q.filter = Sets.newHashSet();
		for (String key : this.filter) {
			q.filter.add(key);
		}
		return q;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ?proj WHERE {\n ");
		for (String constraint : constraintTriples) {
			sb.append(constraint + " \n");
		}
		for (String filterString : filter) {
			sb.append("FILTER (" + filterString + ").\n ");
		}
		sb.append("}");
		//TODO quick fix for reducing processing time assuming result set is smaller than 10
		sb.append("LIMIT 10\n");
		return sb.toString();
	}

	public String toStringWithoutFilter() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		if (filter.isEmpty()) {
			sb.append("?proj ");
		} else {
			for (String projVariable : filter) {
				sb.append("?" + projVariable + " ");

			}
		}
		sb.append(" WHERE {\n ");
		for (String constraint : constraintTriples) {
			sb.append(constraint + " ");
		}
		sb.append("}");
		return sb.toString();
	}

	// FIXME add static to this
	//FIXME http://docs.openlinksw.com/virtuoso/fn_contains.html
	public void addFilterOverAbstractsContraint(String variable, String label, SPARQLQuery q) {
		// TODO quick fix to avoid such queries
		/*
		 * SELECT ?proj WHERE { ?proj a
		 * <http://dbpedia.org/ontology/HorseRider>. ?proj
		 * <http://dbpedia.org/ontology/abstract> ?abstractproj. ?proj
		 * <http://dbpedia.org/ontology/abstract> ?abstractproj. ?const
		 * <http://dbpedia.org/ontology/abstract> ?abstractconst. FILTER
		 * (<bif:contains>(?abstractproj,"'Nobel Prize'")). FILTER
		 * (<bif:contains>(?abstractproj,"influenced")). FILTER
		 * (<bif:contains>(?abstractconst,"'Nobel Prize'")). }
		 */
		for (String filterStatement : filter) {
			if (filterStatement.contains(variable.replace("?", ""))) {
				return;
			}
		}
		q.addConstraint(variable + " <http://dbpedia.org/ontology/abstract> ?abstract" + variable.replace("?", "") + ".");
		String[] whitespaceSeparatedLabel = label.split(" ");
		if (whitespaceSeparatedLabel.length > 1) {
			q.addFilter("<bif:contains>(?abstract" + variable.replace("?", "") + ",\"'" + label + "'\")");
		} else {
			q.addFilter("<bif:contains>(?abstract" + variable.replace("?", "") + ",\"" + label + "\")");
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

	public ArrayList<String> getConstraints() {
		return constraintTriples;
	}
}
