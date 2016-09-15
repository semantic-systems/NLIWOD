package org.aksw.hawk.controller;

import java.util.List;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.nerd.spotter.ASpotter;
import org.aksw.qa.nerd.spotter.Fox;
import org.aksw.qa.nerd.spotter.Spotlight;
import org.aksw.qa.nerd.spotter.TagMe;
import org.apache.jena.query.QueryParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllSpotterOnQALD4 {
	static Logger log = LoggerFactory.getLogger(AllSpotterOnQALD4.class);

	public static void main(final String args[]) {

		List<HAWKQuestion> questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD5_Train_Hybrid));
		for (HAWKQuestion q : questions) {
			log.info(q.getLanguageToQuestion().get("en"));
			try {
				for (ASpotter nerdModule : new ASpotter[] { new Spotlight(), new Fox(), new TagMe() }) {
					q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));
					if (!q.getLanguageToNamedEntites().isEmpty()) {
						for (Entity ent : q.getLanguageToNamedEntites().get("en")) {
							log.info("\t" + nerdModule.toString() + "\t" + ent.toString());
						}
					}
				}
			} catch (QueryParseException e) {
				log.error("QueryParseException: " + q.getPseudoSparqlQuery(), e);
			}
		}
	}
}