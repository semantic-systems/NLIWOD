package org.aksw.mlqa.analyzer.questiontype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

public class QuestionTypeAnalyzer implements IAnalyzer {
	
	@Override
	public Object analyze(String q) {
		if (isASKQuery(q)) {
			return QuestionTypeFeature.BOOLEAN.name();
		} else {
			// FIXME write analysis steps for other types
			return QuestionTypeFeature.RESOURCE.name();

		}
	}

	@Override
	public Attribute getAttribute() {
		return QuestionTypeFeature.attribute;
	}

	// Enumeration values
	enum QuestionTypeFeature {
		LIST, NUMBER, BOOLEAN, RESOURCE;
		private static Attribute attribute = null;
		static {
			ArrayList<String> attributeValues = new ArrayList<String>();
			for (QuestionTypeFeature qtf : QuestionTypeFeature.values()) {
				attributeValues.add(qtf.name());				
			}
			attribute = new Attribute("QuestionTypeFeature", attributeValues);
		};
	}

	// TODO stolen from hawk, please put into qa-commons
	public Boolean isASKQuery(String question) {
		// Compare to source from:
		// src/main/java/org/aksw/hawk/controller/Cardinality.java

		// From train query set: (better to use keyword list!)
		// (Root [-> first child])
		// VBG -> VBZ (Does)
		// VBZ (Is)
		// ADD -> VB (Do)
		// VBP (Are)
		// VBD (Was)
		// VB -> VBD (Did)
		// VBN -> VBD (Was)
		// VB -> VBZ (Does)
		// VBN -> VBZ (Is)

		// regex: ^(Are|D(id|o(es)?)|Is|Was)( .*)$
		return question.startsWith("Are ") || question.startsWith("Did ") || question.startsWith("Do ") || question.startsWith("Does ") || question.startsWith("Is ") || question.startsWith("Was ");
	}
}
