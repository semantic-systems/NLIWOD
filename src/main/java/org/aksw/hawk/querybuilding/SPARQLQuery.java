package org.aksw.hawk.querybuilding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.atlas.logging.Log;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SPARQLQuery implements Cloneable {

	ArrayList<String> constraintTriples = Lists.newArrayList();
	// list of FILTER( ?proj IN (<uri1>,...,<urin>)).
	HashMap<String, List<String>> filter = Maps.newHashMap();

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

	public void addFilter(String projectionVariable, List<String> annotations) {
		if (filter.containsKey(projectionVariable)) {
			List<String> existingAnnotations = filter.get(projectionVariable);
			existingAnnotations.retainAll(annotations);
			filter.put(projectionVariable, existingAnnotations);
		} else {
			filter.put(projectionVariable, Lists.newArrayList(annotations));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Object clone() throws CloneNotSupportedException {
		SPARQLQuery q = new SPARQLQuery();
		q.constraintTriples = (ArrayList<String>) this.constraintTriples.clone();
		q.filter = Maps.newHashMap();
		for(String key:  this.filter.keySet()){
			q.filter.put(key, Lists.newArrayList(this.filter.get(key)));
		}
		return q;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ?proj WHERE {\n ");
		for (String constraint : constraintTriples) {
			sb.append(constraint + " ");
		}
		for (String proj : filter.keySet()) {
			if (!filter.get(proj).isEmpty()) {
				sb.append("FILTER (?" + proj + " IN ( ");
				for (String constraintURI : filter.get(proj)) {
					sb.append("<" + constraintURI + "> , ");
				}
				sb.deleteCharAt(sb.lastIndexOf(",")).append(")).");
			}
		}
		sb.append("}");
		return sb.toString();
	}

	public boolean constraintsContains(String target) {
		for (String c : constraintTriples) {
			if (c.contains(target)) {
				return true;
			}
		}
		return false;
	}
}
