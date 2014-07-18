package org.aksw.hawk.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.module.Fulltexter;
import org.aksw.hawk.module.ModuleBuilder;
import org.aksw.hawk.module.PseudoQueryBuilder;
import org.aksw.hawk.module.SystemAnswerer;
import org.aksw.hawk.nlp.Annotater;
import org.aksw.hawk.nlp.Pruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.util.CachedParseTree;
import org.aksw.hawk.pruner.GraphNonSCCPruner;
import org.aksw.hawk.pruner.QueryVariableHomomorphPruner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class PipelineShortRecall {
	static Logger log = LoggerFactory.getLogger(PipelineShortRecall.class);
	String dataset;
	QALD_Loader datasetLoader;
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
	Fulltexter fulltexter;
	Annotater annotater = new Annotater();

	void run() throws IOException {
		// 1. read in Questions from QALD 4
		List<Question> questions = datasetLoader.load(dataset);
		double overallf = 0;
		double overallp = 0;
		double overallr = 0;
		double counter = 0;

		for (Question q : questions) {
			// by now only work on resource questions
			if (q.answerType.equals("resource") && isSELECTquery(q.pseudoSparqlQuery, q.sparqlQuery)) {
				// log.info("->" + q.languageToQuestion);
				// 2. Disambiguate parts of the query
				q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));

				// 3. Build trees from questions and cache them
				q.tree = cParseTree.process(q);
				// noun combiner, decrease #nodes in the DEPTree
				// decreases
				sentenceToSequence.combineSequences(q);
				// 4. Apply pruning rules

				q.tree = pruner.prune(q);

				// 5. Annotate tree
				log.info(q.languageToQuestion.get("en"));
				annotater.annotateTree(q);
				// log.debug(q.tree.toString());

				// 6. Build queries via subqueries
				SPARQLQueryBuilder queryBuilder = new SPARQLQueryBuilder();
				Map<String, Set<RDFNode>> answer = queryBuilder.build(q);

				// fulltexter.fulltext(q);
				for (String query : answer.keySet()) {
					Set<RDFNode> systemAnswers = answer.get(query);
					// 11. Compare to set of resources from benchmark
					double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
					double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
					double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
					counter++;
					log.debug(query);
					log.debug("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
					overallf += fMeasure;
					overallp += precision;
					overallr += recall;
				}
				// break;
			}
		}
		log.info("Average P=" + overallp / counter + " R=" + overallr / counter + " F=" + overallf / counter + " Counter=" + counter);
	}

	// TODO throw away and work on identifying ask queries
	private boolean isSELECTquery(String pseudoSparqlQuery, String sparqlQuery) {
		if (pseudoSparqlQuery != null) {
			return pseudoSparqlQuery.contains("\nSELECT\n") || pseudoSparqlQuery.contains("SELECT ");
		} else if (sparqlQuery != null) {
			return sparqlQuery.contains("\nSELECT\n") || sparqlQuery.contains("SELECT ");
		}
		return false;
	}

}
