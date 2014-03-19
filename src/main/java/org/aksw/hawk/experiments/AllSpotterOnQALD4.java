package org.aksw.hawk.experiments;

import java.util.List;

import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.controller.PipelineController_QALD4;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.nlp.spotter.Spotlight;
import org.aksw.hawk.nlp.spotter.TagMe;
import org.aksw.hawk.nlp.spotter.WikipediaMiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryParseException;

public class AllSpotterOnQALD4 {
	static Logger log = LoggerFactory.getLogger(PipelineController_QALD4.class);

	public static void main(String args[]) {

		String dataset = ClassLoader.getSystemResource("qald-4_hybrid_train.xml").getFile();
		QaldLoader datasetLoader = new QaldLoader();

		List<Question> questions = datasetLoader.load(dataset);
		for (Question q : questions) {
			log.debug(q.languageToQuestion.get("en"));
			try {
				// 2. Disambiguate parts of the query
				for (ASpotter nerdModule : new ASpotter[] { new Spotlight(), new Fox(), new TagMe(), new WikipediaMiner() }) {
					q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));
					if (!q.languageToNamedEntites.isEmpty()) {
						for (Entity ent : q.languageToNamedEntites.get("en")) {
							log.debug("\t" + nerdModule.toString() + "\t" + ent.toString());
						}
					}
				}
			} catch (QueryParseException e) {
				log.error("QueryParseException: " + q.pseudoSparqlQuery, e);
			}
		}
	}
}