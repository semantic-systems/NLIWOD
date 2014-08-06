package org.aksw.hawk.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.cache.AbstractIndexCache;
import org.aksw.hawk.cache.CachedParseTree;
import org.aksw.hawk.index.DBAbstractsIndex;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.pruner.Pruner;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQLQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class Pipeline {
	static Logger log = LoggerFactory.getLogger(Pipeline.class);
	String dataset;
	QALD_Loader datasetLoader;
	ASpotter nerdModule;
	CachedParseTree cParseTree;
	Pruner pruner;
	SentenceToSequence sentenceToSequence;
	Annotater annotater;
	SPARQLQueryBuilder queryBuilder;

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
				Map<String, Set<RDFNode>> answer = queryBuilder.build(q);

				double fmax = 0;
				double pmax = 0;
				double rmax = 0;
				for (String query : answer.keySet()) {
					log.info(query.substring(0, Math.min(300, query.length())));
					Set<RDFNode> systemAnswers = answer.get(query);
					// 11. Compare to set of resources from benchmark
					double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
					double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
					double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
					if (fMeasure > fmax) {
						log.info("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
						fmax = fMeasure;
						pmax = precision;
						rmax = recall;
					}
				}
				overallf += fmax;
				overallp += pmax;
				overallr += rmax;
				counter++;
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

	public static void main(String args[]) throws IOException {

		for (String file : new String[] { "resources/qald-4_hybrid_train.xml" }) { // ,"resources/qald-4_multilingual_train_withanswers.xml"
			Pipeline controller = new Pipeline();

			log.info("Configuring controller");

			controller.dataset = new File(file).getAbsolutePath();
			controller.datasetLoader = new QALD_Loader();
			controller.nerdModule = new Fox();
			controller.cParseTree = new CachedParseTree();

			AbstractIndexCache cache = new AbstractIndexCache();
			DBAbstractsIndex index = new DBAbstractsIndex(cache);
			controller.sentenceToSequence = new SentenceToSequence(index);
			controller.queryBuilder = new SPARQLQueryBuilder(index);
			controller.annotater = new Annotater(index);

			controller.pruner = new Pruner();
			log.info("Run controller");
			controller.run();
			// TODO if transformed to webapp solve caching more clever
			cache.writeCache();
		}
	}

}
