package org.aksw.hawk.controller;

import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.cache.CachedParseTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.Fox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class QueryTypeClassifier
{
	static Logger log = LoggerFactory.getLogger(QueryTypeClassifier.class);

	public Boolean isASKQuery(Question q) {
		if (log.isDebugEnabled()) {
			log.debug("QUESTION: " + q.languageToQuestion.get("en"));
			MutableTreeNode root = q.tree.getRoot();
			log.debug("ROOT: " + root.posTag);
			if (root.children.size() > 0) {
				MutableTreeNode firstChild = root.children.get(0);
				log.debug("ROOT:CHILD: " + firstChild.posTag);
			}
		}

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

		String query = q.languageToQuestion.get("en").trim();

		// regex: ^(Are|D(id|o(es)?)|Is|Was)( .*)$
		return query.startsWith("Are ") ||
				query.startsWith("Did ") ||
				query.startsWith("Do ") ||
				query.startsWith("Does ") ||
				query.startsWith("Is ") ||
				query.startsWith("Was ");
	}

	public static void main(String args[]) {
		log.info("Test QueryType classification ...");
		log.debug("Initialize components ...");
		QALD_Loader datasetLoader = new QALD_Loader();
		Fox nerdModule = new Fox();
		SentenceToSequence sentenceToSequence = new SentenceToSequence();
		CachedParseTree cParseTree = new CachedParseTree();
		QueryTypeClassifier queryTypeClassifier = new QueryTypeClassifier();

		log.info("Run queries through components ...");
		for (String file : new String[] { "resources/qald-5_train.xml" }) {
			log.debug("Load data file: " + file);
			List<Question> questions = datasetLoader.load(new File(file).getAbsolutePath());

			int counter = 0;
			int counterClassifiedWrong = 0;

			for (Question q : questions) {
				q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));
				sentenceToSequence.combineSequences(q);
				q.tree = cParseTree.process(q);
				log.info(q.tree.toString());
				// Classify query type
				q.isClassifiedAsASKQuery = queryTypeClassifier.isASKQuery(q);

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
