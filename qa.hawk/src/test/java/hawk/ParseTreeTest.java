package hawk;

import java.io.IOException;
import java.util.List;

import org.aksw.hawk.cache.CachedParseTreeClearnlp;
import org.aksw.hawk.cache.Treeprinter;
// import org.aksw.hawk.cache.CachedParseTree;
import org.aksw.hawk.controller.Cardinality;
import org.aksw.hawk.controller.QueryTypeClassifier;
import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.nlp.MutableTreePruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.SentenceToSequenceOpenNLP;
import org.aksw.hawk.nouncombination.NounCombinationChain;
import org.aksw.hawk.nouncombination.NounCombiners;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;
import infrastructure.ServerChecks;

public class ParseTreeTest {
	@BeforeClass
	public static void checkServer() {
		if (!ServerChecks.titanSparqlAlive()) {
			throw new Error("Server down");
		}
	}

	@Test

	public void testProcess() throws IOException {
		Logger log = LoggerFactory.getLogger(ParseTreeTest.class);
		new String();
		new String();
		Spotlight nerdModule = new Spotlight();

		Cardinality cardinality = new Cardinality();

		new SentenceToSequence();

		MutableTreePruner pruner = new MutableTreePruner();

		SPARQL sparql = new SPARQL();
		Annotater annotater = new Annotater(sparql);

		List<HAWKQuestion> questions = null;
		int limiter = 10;
		questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD5_Train_Hybrid)).subList(0, limiter);

		QueryTypeClassifier queryTypeClassifier = new QueryTypeClassifier();

		Treeprinter treeprinter = new Treeprinter();
		CachedParseTreeClearnlp cParseTree2 = new CachedParseTreeClearnlp();

		String[] bareStanford = new String[questions.size()];
		String[] bareClearnlp = new String[questions.size()];
		String[] combinedStanford = new String[questions.size()];
		String[] combinedClearnlp = new String[questions.size()];
		String[] prunedStanford = new String[questions.size()];
		String[] PrunedClearnlp = new String[questions.size()];
		int i = 0;
		StanfordNLPConnector stanfordConnector = new StanfordNLPConnector();
		for (HAWKQuestion q : questions) {
			log.info("Classify question type.");
			q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));

			// Disambiguate parts of the query
			log.info("Named entity recognition.");
			q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));
			Annotation currentAnotation = stanfordConnector.runAnnotation(q.getLanguageToQuestion().get("en"));

			q.setTree(stanfordConnector.process(currentAnotation));
			bareStanford[i] = treeprinter.printTreeStanford(q);
			// log.info("Classify question type.");
			// q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));
			stanfordConnector.parseTree(q, null);
			NounCombinationChain chain = new NounCombinationChain(NounCombiners.StanfordDependecy, NounCombiners.HawkRules);
			chain.runChain(q);

			combinedStanford[i] = treeprinter.printTreeStanford(q);
			// Build trees from questions and cache them
			log.info("Dependency parsing.");
			q.setTree(stanfordConnector.process(currentAnotation));

			// Cardinality identifies the integer i used for LIMIT i
			log.info("Cardinality calculation.");
			q.setCardinality(cardinality.cardinality(q));

			// Apply pruning rules
			log.info("Pruning tree.");
			q.setTree(pruner.prune(q));
			prunedStanford[i] = treeprinter.printTreeStanford(q);
			// Annotate tree
			log.info("Semantically annotating the tree.");
			annotater.annotateTree(q);
			i++;

		}
		treeprinter.closeStanford();

		i = 0;
		questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD5_Train_Hybrid)).subList(0, limiter);
		for (HAWKQuestion q : questions) {
			// log.info("Classify question type.");
			// q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));
			// Disambiguate parts of the query
			// log.info("Named entity recognition.");
			// q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));

			// Build trees from questions and cache them
			log.info("Dependency parsing.");
			q.setTree(cParseTree2.process(q));
			bareClearnlp[i] = treeprinter.printTreeClearnlp(q);

			SentenceToSequenceOpenNLP.combineSequences(q);
			combinedClearnlp[i] = treeprinter.printTreeClearnlp(q);
			// // Cardinality identifies the integer i used for LIMIT i
			// //log.info("Cardinality calculation.");
			// //q.setCardinality(cardinality.cardinality(q));
			//
			// // Apply pruning rules
			// log.info("Pruning tree.");
			q.setTree(pruner.prune(q));
			PrunedClearnlp[i] = treeprinter.printTreeClearnlp(q);
			// // Annotate tree
			// //log.info("Semantically annotating the tree.");
			// //annotater.annotateTree(q);
			//
			// //cParseTree.process(q);
			//
			// // always close the file
			i++;
		}
		treeprinter.closeClearnlp();

		for (int j = 0; j < limiter; j++) {
			bareClearnlp[j] = bareClearnlp[j].replaceAll("[0-9]", "*");
			bareStanford[j] = bareStanford[j].replaceAll("[0-9]", "*");
			combinedClearnlp[j] = combinedClearnlp[j].replaceAll("[0-9]", "*");
			PrunedClearnlp[j] = PrunedClearnlp[j].replaceAll("[0-9]", "*");
			combinedStanford[j] = combinedStanford[j].replaceAll("[0-9]", "*");
			prunedStanford[j] = prunedStanford[j].replaceAll("[0-9]", "*");
			// Removes node numbers, since they often screw up the comparison
			// between otherwise identical trees

			if (!bareClearnlp[j].equals(bareStanford[j])) {
				log.info("Mismatch: Bare Version Question " + j + ":" + questions.get(j).getLanguageToQuestion().get("en"));
				log.info(bareStanford[j]);
				log.info(bareClearnlp[j]);
			}
			if (!combinedClearnlp[j].equals(combinedStanford[j])) {
				log.info("Mismatch: Combined Version Question " + j + ":" + questions.get(j).getLanguageToQuestion().get("en"));
				log.info(combinedStanford[j]);
				log.info(combinedClearnlp[j]);
			}
			if (!PrunedClearnlp[j].equals(prunedStanford[j])) {
				log.info("Mismatch: Pruned Version Question " + j + ":" + questions.get(j).getLanguageToQuestion().get("en"));
				log.info(prunedStanford[j]);
				log.info(PrunedClearnlp[j]);
			}
		}
	}

}
