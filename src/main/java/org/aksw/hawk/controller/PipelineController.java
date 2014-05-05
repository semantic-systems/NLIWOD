package org.aksw.hawk.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.Qald4HybridLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.module.ModuleBuilder;
import org.aksw.hawk.module.PseudoQueryBuilder;
import org.aksw.hawk.module.SystemAnswerer;
import org.aksw.hawk.nlp.NounCombiner;
import org.aksw.hawk.nlp.ParseTree;
import org.aksw.hawk.nlp.Pruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.posTree.TreeTransformer;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.pruner.GraphNonSCCPruner;
import org.aksw.hawk.pruner.QueryVariableHomomorphPruner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class PipelineController {
	static Logger log = LoggerFactory.getLogger(PipelineController.class);
	String dataset;
	Qald4HybridLoader datasetLoader;
	ASpotter nerdModule;
	ParseTree parseTree;
	ModuleBuilder moduleBuilder;
	PseudoQueryBuilder pseudoQueryBuilder;
	Pruner pruner;
	TreeTransformer treeTransform;
	SystemAnswerer systemAnswerer;
	QueryVariableHomomorphPruner queryVariableHomomorphPruner;
	GraphNonSCCPruner graphNonSCCPruner;
	NounCombiner nounCombiner;
	Visualizer vis = new Visualizer();
	SentenceToSequence sentenceToSequence;

	void run() throws IOException {
		// 1. read in Questions from QALD 4
		List<Question> questions = datasetLoader.load(dataset);

		for (Question q : questions) {
			log.info("->" + q.languageToQuestion);
			// 2. Disambiguate parts of the query
			q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));

			// 3. Build trees from questions and cache them
			q.depTree = parseTree.process(q);
			q.tree = treeTransform.DEPtoMutableDEP(q.depTree);

			// noun combiner, so the number of nodes in the DEPTree decreases
			// nounCombiner.combineNouns(q);
			sentenceToSequence.combineSequences(q);
			// visualize the tree
			vis.visTree(q);

			// 4. Apply pruning rules
			q.tree = pruner.prune(q);

			// visualize the tree
			vis.vis(q, nerdModule);
			// 5. Build modules
			q.modules = this.moduleBuilder.build(q);

			// 8. Build pseudo queries
			Iterator<ParameterizedSparqlString> iter = pseudoQueryBuilder.buildQuery(q);
			log.info("Built PseudoQueries");
			queryVariableHomomorphPruner.reset();
			while (iter.hasNext()) {
				ParameterizedSparqlString thisQuery = iter.next();
				if (thisQuery != null) {
					// check whether clauses are connected
					if (graphNonSCCPruner.isSCC(thisQuery)) {
						// homogenize variables in queries
						if (queryVariableHomomorphPruner.queryHasNotBeenHandled(thisQuery)) {
							// 10. Execute queries to generate system answers
							HashMap<String, Set<RDFNode>> answer = systemAnswerer.answer(thisQuery);
							for (String key : answer.keySet()) {
								Set<RDFNode> systemAnswers = answer.get(key);
								// 11. Compare to set of resources from
								// benchmark
								double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
								double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
								double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
								if (fMeasure > 0) {
									log.info("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
									log.info(key);
								}
							}
						}
					}
				}
			}
			vis.horRule();
			System.gc();
		}
		vis.close();
	}
}
