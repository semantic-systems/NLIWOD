package org.aksw.hawk.controller;

import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class QueryTypeClassifier
{
	static Logger log = LoggerFactory.getLogger(QueryTypeClassifier.class);

	public Boolean isASKQuery(String question) {
		// Compare to source from: src/main/java/org/aksw/hawk/controller/Cardinality.java

		// From train query set: (better to use keyword list!)
		// (Root [-> first child])
		// VBG -> VBZ (Does)
		// VBZ        (Is)
		// ADD -> VB  (Do)
		// VBP        (Are)
		// VBD        (Was)
		// VB  -> VBD (Did)
		// VBN -> VBD (Was)
		// VB  -> VBZ (Does)
		// VBN -> VBZ (Is)

		// regex: ^(Are|D(id|o(es)?)|Is|Was)( .*)$
		return question.startsWith("Are ") ||
				question.startsWith("Did ") ||
				question.startsWith("Do ") ||
				question.startsWith("Does ") ||
				question.startsWith("Is ") ||
				question.startsWith("Was ");
	}

	public static void main(String args[]) {
		log.info("Test QueryType classification ...");
		log.debug("Initialize components ...");
		QALD_Loader datasetLoader = new QALD_Loader();
		QueryTypeClassifier queryTypeClassifier = new QueryTypeClassifier();

		log.info("Run queries through components ...");
		for (String file : new String[] { "resources/qald-5_train.xml" }) {
			log.debug("Load data file: " + file);
			List<Question> questions = datasetLoader.load(new File(file).getAbsolutePath());

			int counter = 0;
			int counterClassifiedWrong = 0;

			for (Question q : questions) {
				// Classify query type
				q.isClassifiedAsASKQuery = queryTypeClassifier.isASKQuery(q.languageToQuestion.get("en"));

				++counter;
				if (q.isClassifiedAsASKQuery.booleanValue() != q.loadedAsASKQuery.booleanValue()) {
					log.warn("Expected ASK query classification: " + q.loadedAsASKQuery +
							", got: " + q.isClassifiedAsASKQuery + ", for: " + q.languageToQuestion.get("en"));
					++counterClassifiedWrong;
				}
			}

			log.info("Classified " + counterClassifiedWrong + " wrong from " + counter + " queries.");
		}
	}

}
