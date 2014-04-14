package org.aksw.hawk.controller;

import java.util.List;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.module.ModuleBuilder;
import org.aksw.hawk.module.PseudoQueryBuilder;
import org.aksw.hawk.module.SystemAnswerer;
import org.aksw.hawk.nlp.ParseTree;
import org.aksw.hawk.nlp.Pruner;
import org.aksw.hawk.nlp.posTree.TreeTransformer;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Fox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class PipelineController_QALD4 {
	static Logger log = LoggerFactory.getLogger(PipelineController_QALD4.class);
	private String dataset;
	private QaldLoader datasetLoader;
	private String endpoint;
	private ASpotter nerdModule;
	private ParseTree parseTree;
	private ModuleBuilder moduleBuilder;
	private PseudoQueryBuilder pseudoQueryBuilder;
	private Pruner pruner;
	private TreeTransformer treeTransform;
	private SystemAnswerer systemAnswerer;

	public static void main(String args[]) {
		PipelineController_QALD4 controller = new PipelineController_QALD4();

		log.info("Configuring controller");

		controller.dataset = ClassLoader.getSystemResource("qald-4_hybrid_train.xml").getFile();
		controller.endpoint = "http://dbpedia.org/sparql";
		controller.datasetLoader = new QaldLoader();
		controller.nerdModule = new Fox();
		controller.parseTree = new ParseTree();
		controller.moduleBuilder = new ModuleBuilder();
		controller.pseudoQueryBuilder = new PseudoQueryBuilder();
		controller.pruner = new Pruner();
		controller.treeTransform = new TreeTransformer();
		controller.systemAnswerer = new SystemAnswerer();

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
				q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));
				if (!q.languageToNamedEntites.isEmpty()) {
					log.debug("\t" + Joiner.on("\n").join(q.languageToNamedEntites.get("en")));
				}
				// 3. Build trees from questions and cache them
				q.depTree = this.parseTree.process(q);

				q.tree = this.treeTransform.DEPtoMutableDEP(q.depTree);

				// 4. Apply pruning rules
				q.tree = this.pruner.prune(q);

				// 5. Build modules
				q.modules = this.moduleBuilder.build(q.tree.getRoot(), null, q);

				// TODO 7.1 Apply rdfs reasoning on each module

				// 8. Build pseudo queries
				List<ParameterizedSparqlString> tmp = this.pseudoQueryBuilder.buildQuery(q);

				// TODO 9. Eliminate invalid queries and find top ranked query

				// 10. Execute queries to generate system answers
				if (tmp == null) {
					log.info("\tP=" + 0.0 + " R=" + 0.0 + " F=" + 0.0);
				} else {
					for (ParameterizedSparqlString pseudoQuery : tmp) {
						Set<RDFNode> systemAnswers = this.systemAnswerer.answer(pseudoQuery);

						// 11. Compare to set of resources from benchmark
						double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
						double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
						double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
						log.info("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
					}
				}
			} catch (QueryParseException e) {
				log.error("QueryParseException: " + q.pseudoSparqlQuery, e);
			}
			break;
		}

	}
}
