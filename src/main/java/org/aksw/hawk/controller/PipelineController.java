package org.aksw.hawk.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.QaldLoader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.module.ModuleBuilder;
import org.aksw.hawk.module.Pruner;
import org.aksw.hawk.module.PseudoQueryBuilder;
import org.aksw.hawk.nlp.ParseTree;
import org.aksw.hawk.nlp.posTree.TreeTransformer;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Spotlight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.dependency.DEPTree;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class PipelineController {
	static Logger log = LoggerFactory.getLogger(PipelineController.class);
	private String dataset;
	private QaldLoader datasetLoader;
	private String endpoint;
	private ASpotter nerdModule;
	private ParseTree parseTree;
	private ModuleBuilder moduleBuilder;
	private PseudoQueryBuilder pseudoQueryBuilder;
	private Pruner pruner;
	private TreeTransformer treeTransform;

	public static void main(String args[]) {
		PipelineController controller = new PipelineController();

		log.info("Configuring controller");

		controller.dataset = ClassLoader.getSystemResource("qald-4_hybrid_train.xml").getFile();
		controller.endpoint = "http://dbpedia.org/sparql";
		controller.datasetLoader = new QaldLoader();
		controller.nerdModule = new Spotlight();
		controller.parseTree = new ParseTree();
		controller.moduleBuilder = new ModuleBuilder();
		controller.pseudoQueryBuilder = new PseudoQueryBuilder();
		controller.pruner = new Pruner();
		controller.treeTransform = new TreeTransformer();

		log.info("Run controller");
		controller.run();

	}

	private void run() {
		// 1. read in Questions from QALD 1,2,3,4
		List<Question> questions = datasetLoader.load(dataset);

		for (Question q : questions) {
			log.debug("->" + q.languageToQuestion);
			try {
				// 2. Disambiguate parts of the query
				q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));
				if (!q.languageToNamedEntites.isEmpty()) {
					for (Entity ent : q.languageToNamedEntites.get("en")) {
						log.debug("\t" + ent.toString());
					}
				}
				// 3. Build trees from questions and cache them
				DEPTree tmpTree = this.parseTree.process(q);
				q.tree = this.treeTransform.DEPtoMutableDEP(tmpTree);

				// 4. Apply pruning rules
				log.debug(q.tree.toString());
				q.tree = this.pruner.prune(q);
				log.debug(q.tree.toString());

				// TODO 5. Find projection variable

				// 7. Build modules
				q.modules = this.moduleBuilder.build(q.tree.getRoot(), null, q);

				// TODO 7.1 Apply rdfs reasoning on each module

				// 8. Build pseudo queries
				List<ParameterizedSparqlString> tmp = this.pseudoQueryBuilder.buildQuery(q);
				int i = 0;
				for (ParameterizedSparqlString pseudoQuery : tmp) {
					log.debug("\t " + i++ + " : " + pseudoQuery.toString());
				}

				// TODO 9. Eliminate invalid queries and find top ranked query
				q.pseudoSystemQuery = null;
				// TODO 10. Execute queries to generate system answers
				Set<RDFNode> systemAnswers = new HashSet<RDFNode>();

				// 11. Compare to set of resources from benchmark
				double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
				double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
				double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
				log.debug("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
				break;
			} catch (QueryParseException e) {
				log.error("QueryParseException: " + q.pseudoSparqlQuery, e);
			}
		}

	}

}
