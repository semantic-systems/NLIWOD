package org.aksw.hawk.controller;

import java.io.File;
import java.io.IOException;

import org.aksw.autosparql.commons.qald.Qald4HybridLoader;
import org.aksw.hawk.module.ModuleBuilder;
import org.aksw.hawk.module.PseudoQueryBuilder;
import org.aksw.hawk.module.SystemAnswerer;
import org.aksw.hawk.nlp.NounCombiner;
import org.aksw.hawk.nlp.ParseTree;
import org.aksw.hawk.nlp.Pruner;
import org.aksw.hawk.nlp.posTree.TreeTransformer;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.pruner.GraphNonSCCPruner;
import org.aksw.hawk.pruner.QueryVariableHomomorphPruner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QALD4_hybrid {
	static Logger log = LoggerFactory.getLogger(QALD4_hybrid.class);

	public static void main(String args[]) throws IOException {
		PipelineController controller = new PipelineController();

		log.info("Configuring controller");

		controller.dataset = new File(args[0]).getAbsolutePath();
		controller.datasetLoader = new Qald4HybridLoader();
		controller.nerdModule = new Fox();
		controller.parseTree = new ParseTree();
		controller.treeTransform = new TreeTransformer();
		controller.nounCombiner = new NounCombiner();
		controller.pruner = new Pruner();
		controller.moduleBuilder = new ModuleBuilder();
		controller.pseudoQueryBuilder = new PseudoQueryBuilder();
		controller.queryVariableHomomorphPruner = new QueryVariableHomomorphPruner();
		controller.graphNonSCCPruner = new GraphNonSCCPruner();
		String endpoint = "http://dbpedia.org/sparql";
		controller.systemAnswerer = new SystemAnswerer(endpoint, controller.nerdModule);

		log.info("Run controller");
		controller.run();

	}
}
