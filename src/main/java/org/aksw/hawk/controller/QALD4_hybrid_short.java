package org.aksw.hawk.controller;

import java.io.File;
import java.io.IOException;

import org.aksw.autosparql.commons.qald.Qald4HybridLoader;
import org.aksw.hawk.module.Fulltexter;
import org.aksw.hawk.module.ModuleBuilder;
import org.aksw.hawk.module.PseudoQueryBuilder;
import org.aksw.hawk.module.SystemAnswerer;
import org.aksw.hawk.nlp.Pruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.nlp.util.CachedParseTree;
import org.aksw.hawk.pruner.GraphNonSCCPruner;
import org.aksw.hawk.pruner.QueryVariableHomomorphPruner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QALD4_hybrid_short {
	static Logger log = LoggerFactory.getLogger(QALD4_hybrid_short.class);

	public static void main(String args[]) throws IOException {
		PipelineShortRecall controller = new PipelineShortRecall();

		log.info("Configuring controller");

		controller.dataset = new File(args[0]).getAbsolutePath();
		controller.datasetLoader = new Qald4HybridLoader();
		controller.nerdModule = new Fox();
		controller.cParseTree = new CachedParseTree();
		controller.sentenceToSequence = new SentenceToSequence();
		controller.pruner = new Pruner();
		controller.moduleBuilder = new ModuleBuilder();
		controller.pseudoQueryBuilder = new PseudoQueryBuilder();
		controller.queryVariableHomomorphPruner = new QueryVariableHomomorphPruner();
		controller.graphNonSCCPruner = new GraphNonSCCPruner();
		String endpoint = "http://dbpedia.org/sparql";
		controller.systemAnswerer = new SystemAnswerer(endpoint, controller.nerdModule);
		controller.fulltexter = new Fulltexter();
		log.info("Run controller");
		controller.run();

	}
}
