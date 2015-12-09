package org.aksw.mlqa.analyzer;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

//TODO write unit test for this analyzer
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
			FastVector attributeValues = new FastVector(QuestionTypeFeature.values().length);
			for (QuestionTypeFeature qtf : QuestionTypeFeature.values()) {
				attributeValues.addElement(qtf.name());
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

	// TODO transform to unit test
	// public static void main(String args[]) {
	// log.info("Test QueryType classification ...");
	// log.debug("Initialize components ...");
	// QALD_Loader datasetLoader = new QALD_Loader();
	// QueryTypeClassifier queryTypeClassifier = new QueryTypeClassifier();
	//
	// log.info("Run queries through components ...");
	// for (String file : new String[] { "resources/qald-5_train.xml" }) {
	// log.debug("Load data file: " + file);
	// List<Question> questions = datasetLoader.load(new
	// File(file).getAbsolutePath());
	//
	// int counter = 0;
	// int counterASK = 0;
	// int counterClassifiedWrong = 0;
	//
	// for (Question q : questions) {
	// // Classify query type
	// q.isClassifiedAsASKQuery =
	// queryTypeClassifier.isASKQuery(q.languageToQuestion.get("en"));
	//
	// if (log.isDebugEnabled()) {
	// log.debug("Question ID=" + q.id + ": isASK=" + q.isClassifiedAsASKQuery +
	// " - " + q.languageToQuestion.get("en"));
	// }
	//
	// if (q.isClassifiedAsASKQuery) {
	// ++counterASK;
	// }
	//
	// ++counter;
	// if (q.isClassifiedAsASKQuery.booleanValue() !=
	// q.loadedAsASKQuery.booleanValue()) {
	// log.warn("Expected ASK query classification: " + q.loadedAsASKQuery +
	// ", got: " + q.isClassifiedAsASKQuery + ", for: " +
	// q.languageToQuestion.get("en"));
	// ++counterClassifiedWrong;
	// }
	// }
	//
	// log.info("Classified " + counterClassifiedWrong + " wrong from " +
	// counter + " queries. (" + counterASK + " are ASK)");
	// }
	// }

}
