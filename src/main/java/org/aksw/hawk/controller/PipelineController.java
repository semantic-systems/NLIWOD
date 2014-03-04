package org.aksw.hawk.controller;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.nlp.Fox_NER_NED;
import org.aksw.hawk.nlp.NERD_module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class PipelineController {
	static Logger log = LoggerFactory.getLogger(PipelineController.class);
	private String dataset;
	private QaldLoader datasetLoader;
	private String endpoint;
	private NERD_module nerdModule;

	public static void main(String args[]) {
		PipelineController controller = new PipelineController();

		log.info("Configuring controller");
		controller.setDataset(ClassLoader.getSystemResource("qald-4_hybrid_train.xml"));
		controller.setDatasetLoader(new QaldLoader());
		controller.setEndpoint("http://dbpedia.org/sparql");
		controller.setNERDmodule(new Fox_NER_NED());
		log.info("Run controller");
		controller.run();

	}

	private void run() {
		// 1. read in Questions from QALD 1,2,3,4
		List<Question> questions = datasetLoader.load(dataset);

		for (Question q : questions) {
			log.info("->" + q.languageToQuestion);
			try {
				// 2. Disambiguate parts of the query
				Map<String, List<Entity>> entities = nerdModule.getEntities(q.languageToQuestion.get("en"));
				for (Entity ent : entities.get("en")) {
					log.info("\t" + ent.toString());
				}
				// TODO 3. Build trees from questions and cache them

				// TODO 4. Apply pruning rules

				// TODO 5. Find projection variable

				// TODO 7. Build modules

				// TODO 7.1 Apply rdfs reasoning on each module

				// TODO 8. Build SPARQL queries
				Set<RDFNode> systemAnswers = new HashSet<RDFNode>();

				// TODO 9. Eliminate invalid queries and find top ranked query

				// 10. Compare to set of resources from benchmark

				double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
				double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
				double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
				log.info("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
			} catch (QueryParseException e) {
				log.error("QueryParseException: " + q.sparqlQuery, e);
			}
		}

	}

	private void setDataset(URL url) {
		this.dataset = url.getFile();
	}

	private void setDatasetLoader(QaldLoader qaldLoader) {
		this.datasetLoader = qaldLoader;

	}

	private void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	private void setNERDmodule(NERD_module nerd) {
		this.nerdModule = nerd;
	}
}
