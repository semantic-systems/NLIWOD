package org.aksw.hawk.controller;

import java.net.URL;
import java.util.List;

import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineController {
	static Logger log = LoggerFactory.getLogger(PipelineController.class);
	private String dataset;
	private QaldLoader datasetLoader;

	public static void main(String args[]) {
		PipelineController controller = new PipelineController();

		log.info("Configuring controller");
		controller.setDataset(ClassLoader.getSystemResource("qald-4_hybrid_train.xml"));
		controller.setDatasetLoader(new QaldLoader());

		log.info("Run controller");
		controller.run();

	}

	

	private void run() {
		// 1. read in Questions from QALD 1,2,3,4
		List<Question> questions = datasetLoader.load(dataset);
		
		// 1.1 Disambiguate parts of the query

		// 2. Build trees from questions and cache them

		// 3. For each tree

		// 3.2 Apply pruning rules

		// 3.3 Find projection variable

		// 3.4 Build modules

		// 3.5 For each module

		// 3.5.1 Apply rdfs reasoning on each module

		// 3.6 Build SPARQL queries

		// 3.7 Eliminate invalid queries and find top ranked query

		// 4. Compare to set of resources from benchmark
	}

	private void setDataset(URL url) {
		this.dataset = url.getFile();
	}
	private void setDatasetLoader(QaldLoader qaldLoader) {
		this.datasetLoader = qaldLoader;
		
	}
}
