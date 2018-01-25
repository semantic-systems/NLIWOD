package org.aksw.hawk.datastructures;


import java.util.Set;

import org.aksw.qa.commons.sparql.SPARQLQuery;
import org.apache.jena.rdf.model.RDFNode;
import org.json.simple.JSONObject;


public class Answer implements Comparable<Answer> {

	public Set<RDFNode> answerSet;
	public SPARQLQuery query;
	public String queryString;
	public String question;
	public String question_id;
	public Double score = 1.0;
	public JSONObject answerAsJson;
	
	@Override
	public int compareTo(final Answer a) {
		return Double.compare(this.score, a.score);
	}
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Answer other = (Answer) obj;
		if (answerSet == null) {
			if (other.answerSet != null)
				return false;
		} else if (!answerSet.equals(other.answerSet))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answerSet == null) ? 0 : answerSet.hashCode());
		return result;
	}


	@Override
	public String toString() {
		return "Answer [answerSet=" + answerSet + ", score=" + score + ", queryString=" + queryString + "]";
	}

}