package org.aksw.hawk.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.Qald4HybridLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.index.DBAbstractsIndex;
import org.aksw.hawk.module.ModuleBuilder;
import org.aksw.hawk.module.PseudoQueryBuilder;
import org.aksw.hawk.module.SystemAnswerer;
import org.aksw.hawk.nlp.Pruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.util.CachedParseTree;
import org.aksw.hawk.pruner.GraphNonSCCPruner;
import org.aksw.hawk.pruner.QueryVariableHomomorphPruner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class ShortRecallPipeline {
	static Logger log = LoggerFactory.getLogger(ShortRecallPipeline.class);
	String dataset;
	Qald4HybridLoader datasetLoader;
	ASpotter nerdModule;
	CachedParseTree cParseTree;
	ModuleBuilder moduleBuilder;
	PseudoQueryBuilder pseudoQueryBuilder;
	Pruner pruner;
	SystemAnswerer systemAnswerer;
	QueryVariableHomomorphPruner queryVariableHomomorphPruner;
	GraphNonSCCPruner graphNonSCCPruner;
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
			q.tree = cParseTree.process(q);
			// noun combiner, so the number of nodes in the DEPTree decreases
			sentenceToSequence.combineSequences(q);

			// 4. Apply pruning rules
			q.tree = pruner.prune(q);
			log.info(q.tree.toString());
			
			HashMap<String, Set<RDFNode>> answer = fulltext(q);
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

	private HashMap<String, Set<RDFNode>> fulltext(Question q) {
		HashMap<String, Set<RDFNode>> map = Maps.newHashMap();
		DBAbstractsIndex index = new DBAbstractsIndex();
		Set<RDFNode> set = Sets.newHashSet();
		Stack<MutableTreeNode> stack = new Stack<MutableTreeNode>();
		stack.push(q.tree.getRoot());
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			if (tmp.posTag.equals("CombinedNN")) {
				for (String resourceURL : index.listAbstractsContaining(tmp.label)) {
					set.add(new ResourceImpl(resourceURL));
				}
			}
			for (MutableTreeNode child : tmp.getChildren()) {
				stack.push(child);
			}
		}
		map.put(q.languageToQuestion.get("en"), set);
		index.close();
		return map;
	}
}
