package org.aksw.hawk.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GerbilResults {
	private Logger log = LoggerFactory.getLogger(GerbilResults.class);
	private Vector<HashMap<String,GerbilURI>> bindings;

	public GerbilResults() {
		bindings = new Vector<>();
	}

	public Vector<HashMap<String,GerbilURI>> getBindings() {
		return bindings;
	}

	public GerbilResults setBindings(final HAWKQuestion q) {
		//GerbilBinding binding = new GerbilBinding();
		if (q.getFinalAnswer() != null && !q.getFinalAnswer().isEmpty()) {
			log.info("Answer list: "+ q.getFinalAnswer().get(0).answerSet);
			for (RDFNode answer : q.getFinalAnswer().get(0).answerSet) {
				HashMap<String,GerbilURI> dictForURI = new HashMap<>();
				GerbilURI uri = new GerbilURI();
				uri.setValue(answer.asResource().getURI());
				uri.setType("uri");
				dictForURI.put("uri", uri);
				bindings.add(dictForURI);
			}
		}
		log.info("Object being returned from setURI func: " + this.toString());
		//binding.setURI(q);
		//bindings.add(binding);
		return this;
	}

	@Override
	public String toString() {
		return "\n      " + Objects.toString(bindings);
	}

}
