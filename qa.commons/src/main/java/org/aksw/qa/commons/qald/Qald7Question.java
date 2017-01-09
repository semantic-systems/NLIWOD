package org.aksw.qa.commons.qald;

import java.util.Set;

import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.apache.jena.ext.com.google.common.base.Strings;

public class Qald7Question extends Question {
	private Dataset fromDataset;
	private Set<String> serverAnswers;
	private Set<Fail> fails;

	public Set<Fail> getFails() {
		return fails;
	}

	public void setFails(final Set<Fail> fails) {
		this.fails = fails;
	}

	public void addFail(final Fail fail) {
		if (fail != null) {
			this.fails.add(fail);
		}
	}

	public Dataset getFromDataset() {
		return fromDataset;
	}

	public Set<String> getServerAnswers() {
		return serverAnswers;
	}

	public void setFromDataset(final Dataset fromDataset) {
		this.fromDataset = fromDataset;
	}

	public void setServerAnswers(final Set<String> serverAnswers) {
		this.serverAnswers = serverAnswers;
	}

	@Override
	public int hashCode() {
		String q = this.getLanguageToQuestion().get("en");
		if (Strings.isNullOrEmpty(q)) {
			return System.identityHashCode(this);
		}

		return q.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		String q = this.getLanguageToQuestion().get("en");
		if (obj == this) {
			return true;
		}
		if (Strings.isNullOrEmpty(q)) {
			return false;
		}
		if (!(obj instanceof Qald7Question)) {
			return false;
		}
		Qald7Question strObj = (Qald7Question) obj;
		String q2 = strObj.getLanguageToQuestion().get("en");
		if (Strings.isNullOrEmpty(q2)) {
			return false;
		}
		return q.equals(q2);
	}
}
