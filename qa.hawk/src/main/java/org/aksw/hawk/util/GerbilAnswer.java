package org.aksw.hawk.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.load.json.*;

public class GerbilAnswer {
	private Logger log = LoggerFactory.getLogger(GerbilAnswer.class);
	private HashMap<String, ArrayList<String>> head;
	private GerbilResults results;
	
	public GerbilAnswer(){
		head = new HashMap<>();
		results = new GerbilResults();
	}
	
	public GerbilResults getResults() {
		if (results == null) {
			results = new GerbilResults();
		}
		return results;
	}

	public HashMap<String, ArrayList<String>> getHead() {
		return this.head;
	}
	
	public GerbilAnswer setHead(final HAWKQuestion q) {
		ArrayList<String> ansvars = new ArrayList<>();
		ansvars.add(q.getAnswerType());
		this.head.put("vars", ansvars);
		return this;
	}
	public GerbilAnswer setResults(final HAWKQuestion q) {
		this.results.setBindings(q);
		return this;

	}
	@Override
	public String toString() {
		return "\n    Head: " + Objects.toString(head) + "\n    Results: " + Objects.toString(results);
	}
}
