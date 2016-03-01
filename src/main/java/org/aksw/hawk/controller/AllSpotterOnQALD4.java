package org.aksw.hawk.controller;

import java.util.List;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.nlp.spotter.Spotlight;
import org.aksw.hawk.nlp.spotter.TagMe;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryParseException;

public class AllSpotterOnQALD4 {
	static Logger log = LoggerFactory.getLogger(AllSpotterOnQALD4.class);

	public static void main(String args[]) {

		QALD_Loader datasetLoader = new QALD_Loader();

		List<IQuestion> questions = datasetLoader.load(Dataset.QALD5_Train);
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