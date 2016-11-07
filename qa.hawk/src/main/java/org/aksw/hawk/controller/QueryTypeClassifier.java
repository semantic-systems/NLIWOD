package org.aksw.hawk.controller;

import java.util.List;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryTypeClassifier {
	static Logger log = LoggerFactory.getLogger(QueryTypeClassifier.class);

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

	public static void main(String args[]) {
		log.info("Test QueryType classification ...");
		log.debug("Initialize components ...");
		LoaderController datasetLoader = new LoaderController();
		QueryTypeClassifier queryTypeClassifier = new QueryTypeClassifier();

		log.info("Run queries through components ...");
		for (Dataset d : Dataset.values()) {
			log.debug("Load data file: " + d);
			List<HAWKQuestion> questions = HAWKQuestionFactory.createInstances(datasetLoader.load(d));
			int counter = 0;
			int counterASK = 0;
			int counterClassifiedWrong = 0;

			for (HAWKQuestion q : questions) {
				// Classify query type
				q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));

				if (log.isDebugEnabled()) {
					log.debug("Question ID=" + q.getId() + ": isASK=" + q.getIsClassifiedAsASKQuery() + " - " + q.getLanguageToQuestion().get("en"));
				}

				if (q.getIsClassifiedAsASKQuery()) {
					++counterASK;
				}

				++counter;
				if (q.getIsClassifiedAsASKQuery().booleanValue() != q.getLoadedAsASKQuery().booleanValue()) {
					log.warn("Expected ASK query classification: " + q.getLoadedAsASKQuery() + ", got: " + q.getIsClassifiedAsASKQuery() + ", for: " + q.getLanguageToQuestion().get("en"));
					++counterClassifiedWrong;
				}
			}

			log.info("Classified " + counterClassifiedWrong + " wrong from " + counter + " queries. (" + counterASK + " are ASK)");
		}
	}
}
