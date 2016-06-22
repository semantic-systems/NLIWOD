package org.aksw.hawk.datastructures;

import java.util.Set;

import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.apache.jena.rdf.model.RDFNode;

//FIXME mach eine ordentliche toString()
public class Answer implements Comparable<Answer> {

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answerSet == null) ? 0 : answerSet.hashCode());
		return result;
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

	public Set<RDFNode> answerSet;
	public SPARQLQuery query;
	public Double score = 1.0;
	public String question;
	public Integer question_id;
	public String queryString;

	@Override
	public int compareTo(final Answer a) {
		return Double.compare(this.score, a.score);
	}

	@Override
	public String toString() {
		return "Answer [answerSet=" + answerSet + ", score=" + score + ", queryString=" + queryString + "]";
	}

	public SPARQLQuery toSPARQLQuery() {
		// TODO Auto-generated method stub
		return null;
	}

}