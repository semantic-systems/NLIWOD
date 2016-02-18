package org.aksw.hawk.controller;

import java.util.List;

import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.datastructures.Question;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.nlp.spotter.Spotlight;
import org.aksw.hawk.nlp.spotter.TagMe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryParseException;

public class AllSpotterOnQALD4 {
	static Logger log = LoggerFactory.getLogger(AllSpotterOnQALD4.class);

	public static void main(String args[]) {

		String dataset = "resources/qald-4_hybrid_train.xml";
		QALD_Loader datasetLoader = new QALD_Loader();

		List<Question> questions = datasetLoader.load(dataset);
		for (Question q : questions) {
			log.info(q.languageToQuestion.get("en"));
			try {
				for (ASpotter nerdModule : new ASpotter[] { new Spotlight(), new Fox(), new TagMe() }) {
					q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));
					if (!q.languageToNamedEntites.isEmpty()) {
						for (Entity ent : q.languageToNamedEntites.get("en")) {
							log.info("\t" + nerdModule.toString() + "\t" + ent.toString());
						}
					}
				}
			} catch (QueryParseException e) {
				log.error("QueryParseException: " + q.pseudoSparqlQuery, e);
			}
		}
	}
}