package org.aksw.mlqa.analyzer;

import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.logging.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

//TODO write unit test for this analyzer
/**
 * Analyzes which
 * 
 * @author ricardousbeck
 *
 */
public class QueryResourceTypeAnalyzer implements IAnalyzer {
	Logger log = LoggerFactory.getLogger(QueryResourceTypeAnalyzer.class);
	private Attribute attribute = null;
	private Fox fox;

	public QueryResourceTypeAnalyzer() {
		FastVector attributeValues = new FastVector();
		attributeValues.addElement("Place");
		attributeValues.addElement("Person");
		attributeValues.addElement("Organization");
		attributeValues.addElement("Misc");
		attribute = new Attribute("QueryResourceType", attributeValues);

		this.fox = new Fox();
	}

	@Override
	public Object analyze(String q) {
		log.debug("String question: " + q);

		Map<String, List<Entity>> entities = fox.getEntities(q);
		log.debug(entities.toString());
		return "Place";

	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}

}
