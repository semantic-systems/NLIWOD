package hawk;

import java.io.IOException;
import java.util.List;

import org.aksw.hawk.cache.CachedParseTreeClearnlp;
import org.aksw.hawk.cache.Treeprinter;
//import org.aksw.hawk.cache.CachedParseTree;
import org.aksw.hawk.controller.Cardinality;
import org.aksw.hawk.controller.QueryTypeClassifier;
import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.nlp.MutableTreePruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.pipeline.Annotation;

public class ParseTreeTest {

	@Test
	public void testProcess() throws IOException {
		Logger log = LoggerFactory.getLogger(ParseTreeTest.class);
		String stanfordTree = new String();
		String clearNLPTree = new String();
		Fox nerdModule = new Fox();

		Cardinality cardinality = new Cardinality();

		SentenceToSequence sentenceToSequence = new SentenceToSequence();

		MutableTreePruner pruner = new MutableTreePruner();

		SPARQL sparql = new SPARQL();
		Annotater annotater = new Annotater(sparql);

		List<HAWKQuestion> questions = null;
		int limiter = 10;
		questions = HAWKQuestionFactory.createInstances(QALD_Loader.load(Dataset.QALD5_Test)).subList(0, limiter);

		QueryTypeClassifier queryTypeClassifier = new QueryTypeClassifier();

		Treeprinter treeprinter = new Treeprinter();
		CachedParseTreeClearnlp cParseTree2 = new CachedParseTreeClearnlp();

		String[] BareStanford = new String[questions.size()];
		String[] BareClearnlp = new String[questions.size()];
		String[] CombinedStanford = new String[questions.size()];
		String[] CombinedClearnlp = new String[questions.size()];
		String[] PrunedStanford = new String[questions.size()];
		String[] PrunedClearnlp = new String[questions.size()];
		int i = 0;
		StanfordNLPConnector stanfordConnector = new StanfordNLPConnector();
		for (HAWKQuestion q : questions) {
			log.info("Classify question type.");
			q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));

			// Disambiguate parts of the query
			log.info("Named entity recognition.");
			q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));
			Annotation currentAnotation = stanfordConnector.runAnnotation(q);
			q.setTree(stanfordConnector.process(currentAnotation));
			BareStanford[i] = treeprinter.printTreeStanford(q);
			// log.info("Classify question type.");
			// q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));
			stanfordConnector.combineSequences(currentAnotation, q);
			q.setTree(stanfordConnector.process(currentAnotation));
			currentAnotation = stanfordConnector.runAnnotation(q);

			CombinedStanford[i] = treeprinter.printTreeStanford(q);
			// Build trees from questions and cache them
			log.info("Dependency parsing.");
			q.setTree(stanfordConnector.process(currentAnotation));

			// Cardinality identifies the integer i used for LIMIT i
			log.info("Cardinality calculation.");
			q.setCardinality(cardinality.cardinality(q));

			// Apply pruning rules
			log.info("Pruning tree.");
			q.setTree(pruner.prune(q));
			PrunedStanford[i] = treeprinter.printTreeStanford(q);
			// Annotate tree
			log.info("Semantically annotating the tree.");
			annotater.annotateTree(q);
			i++;

		}
		treeprinter.closeStanford();

		i = 0;
		questions = HAWKQuestionFactory.createInstances(QALD_Loader.load(Dataset.QALD5_Test)).subList(0, limiter);
		for (HAWKQuestion q : questions) {
			// log.info("Classify question type.");
			// q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));
			// Disambiguate parts of the query
			// log.info("Named entity recognition.");
			// q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));

			// Build trees from questions and cache them
			log.info("Dependency parsing.");
			q.setTree(cParseTree2.process(q));
			BareClearnlp[i] = treeprinter.printTreeClearnlp(q);
			sentenceToSequence.combineSequences(q);
			CombinedClearnlp[i] = treeprinter.printTreeClearnlp(q);
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
			BareClearnlp[j] = BareClearnlp[j].replaceAll("[0-9]", "*");
			BareStanford[j] = BareStanford[j].replaceAll("[0-9]", "*");
			CombinedClearnlp[j] = CombinedClearnlp[j].replaceAll("[0-9]", "*");
			PrunedClearnlp[j] = PrunedClearnlp[j].replaceAll("[0-9]", "*");
			CombinedStanford[j] = CombinedStanford[j].replaceAll("[0-9]", "*");
			PrunedStanford[j] = PrunedStanford[j].replaceAll("[0-9]", "*");
			// Removes node numbers, since they often screw up the comparison
			// between otherwise identical trees

			if (!BareClearnlp[j].equals(BareStanford[j])) {
				log.info("Mismatch: Bare Version Question " + j + ":" + questions.get(j).getLanguageToQuestion().get("en"));
				log.info(BareStanford[j]);
				log.info(BareClearnlp[j]);
			}
			if (!CombinedClearnlp[j].equals(CombinedStanford[j])) {
				log.info("Mismatch: Combined Version Question " + j + ":" + questions.get(j).getLanguageToQuestion().get("en"));
				log.info(CombinedStanford[j]);
				log.info(CombinedClearnlp[j]);
			}
			if (!PrunedClearnlp[j].equals(PrunedStanford[j])) {
				log.info("Mismatch: Pruned Version Question " + j + ":" + questions.get(j).getLanguageToQuestion().get("en"));
				log.info(PrunedStanford[j]);
				log.info(PrunedClearnlp[j]);
			}
		}
	}

}
