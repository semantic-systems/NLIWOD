package org.aksw.hawk.experiment;

import java.util.ArrayList;
import java.util.List;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.nouncombination.NounCombinationChain;
import org.aksw.hawk.nouncombination.NounCombiners;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.nerd.spotter.Spotlight;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.ext.com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NounPhraseIdentification {
	static Logger log = LoggerFactory.getLogger(NounPhraseIdentification.class);

	public static void main(final String[] args) {
		// initialize both noun phrase combiners

		// load questions

		// for each question

		// -- calculate noun phrases with both combiners
		// -- extract noun phrases from questions via SPARQL text:query
		// attribute
		// -- measure p,r,f

		// /---------------

		StanfordNLPConnector connector = new StanfordNLPConnector();
		Spotlight nerdModule = new Spotlight();
		List<IQuestion> loadedQuestions = LoaderController.load(Dataset.QALD6_Train_Hybrid);
		List<HAWKQuestion> questionsStanford = HAWKQuestionFactory.createInstances(loadedQuestions);

		NounCombinationChain hawk = new NounCombinationChain(NounCombiners.HawkRules);
		NounCombinationChain stanf = new NounCombinationChain(NounCombiners.StanfordDependecy);

		NounCombinationChain hawkStanf = new NounCombinationChain(NounCombiners.HawkRules, NounCombiners.StanfordDependecy);
		NounCombinationChain stanfHawk = new NounCombinationChain(NounCombiners.StanfordDependecy, NounCombiners.HawkRules);

		for (HAWKQuestion currentQuestion : questionsStanford) {

			String s = "Which birds are protected under the National Parks and Wildlife Act?";
			if (!currentQuestion.getLanguageToQuestion().get("en").equals(s)) {
				continue;
			}
			log.info(currentQuestion.getLanguageToQuestion().get("en"));
			currentQuestion.setLanguageToNamedEntites(nerdModule.getEntities(currentQuestion.getLanguageToQuestion().get("en")));
			connector.parseTree(currentQuestion, null);
			System.out.println(currentQuestion.getTree().toString());
			ArrayList<ImmutablePair<String, HAWKQuestion>> annoQuestions = new ArrayList<>();
			HAWKQuestion qHawk = hawk.runChainReturnNew(currentQuestion);
			annoQuestions.add(new ImmutablePair<>("OwnRules:      ", qHawk));
			HAWKQuestion qStanf = stanf.runChainReturnNew(currentQuestion);
			annoQuestions.add(new ImmutablePair<>("Stanford:      ", qStanf));
			HAWKQuestion qHawkStanf = hawkStanf.runChainReturnNew(currentQuestion);
			annoQuestions.add(new ImmutablePair<>("1.Own 2.Stanf: ", qHawkStanf));
			HAWKQuestion qStanfHawk = stanfHawk.runChainReturnNew(currentQuestion);
			annoQuestions.add(new ImmutablePair<>("1.Stanf 2.Own  ", qStanfHawk));

			for (ImmutablePair<String, HAWKQuestion> it : annoQuestions) {
				if ((it.getRight().getLanguageToNounPhrases().get("en") != null) && !it.getRight().getLanguageToNounPhrases().get("en").isEmpty()) {
					ArrayList<String> nouns = new ArrayList<>();
					for (Entity entity : it.getRight().getLanguageToNounPhrases().get("en")) {
						nouns.add(entity.getLabel());
					}
					log.info(it.getLeft() + "[" + Joiner.on(", ").join(nouns) + "]");
				}
			}
			log.info("\n");

		}

		// NounCombinationChain stanfordNNChain = new
		// NounCombinationChain(NounCombiners.StanfordDependecy);
		//
		// StanfordNLPConnector connector = new StanfordNLPConnector();
		// for (HAWKQuestion currentQuestion : questionsStanford) {
		// log.info(currentQuestion.getLanguageToQuestion().get("en"));
		// currentQuestion.setLanguageToNamedEntites(nerdModule.getEntities(currentQuestion.getLanguageToQuestion().get("en")));
		// // Annotation doc = stanford.runAnnotation(currentQuestion);
		// /**
		// * Note: StanfordConnector.combineSequences() doesnt run Noun phrase
		// * combining anymore. Use NounCombinationChain for this.
		// */
		// connector.parseTree(currentQuestion);
		// stanfordNNChain.runChain(currentQuestion);
		//
		// // stanford.combineSequences(doc, currentQuestion);
		// Map<String, List<Entity>> languageToNounPhrases =
		// currentQuestion.getLanguageToNounPhrases();
		// if ((languageToNounPhrases != null) &&
		// !languageToNounPhrases.isEmpty()) {
		// log.info("Stanford:" + Joiner.on(",
		// ").skipNulls().join(languageToNounPhrases.get("en")));
		// }
		// }
		// List<HAWKQuestion> questionsClear =
		// HAWKQuestionFactory.createInstances(loadedQuestions);
		//
		// for (HAWKQuestion currentQuestion : questionsClear) {
		// log.info(currentQuestion.getLanguageToQuestion().get("en"));
		// currentQuestion.setLanguageToNamedEntites(nerdModule.getEntities(currentQuestion.getLanguageToQuestion().get("en")));
		// SentenceToSequence.combineSequences(currentQuestion);
		//
		// Map<String, List<Entity>> languageToNounPhrases =
		// currentQuestion.getLanguageToNounPhrases();
		// if ((languageToNounPhrases != null) &&
		// !languageToNounPhrases.isEmpty()) {
		// log.info("Clear:" + Joiner.on(",
		// ").skipNulls().join(languageToNounPhrases.get("en")));
		// }
		// }

		// try {
		//
		// File file = new File("stanford_compound.txt");
		//
		// file.createNewFile();
		//
		// FileWriter fw = new FileWriter(file.getAbsoluteFile());
		// BufferedWriter bw = new BufferedWriter(fw);
		// // !!!!bw.write(out.toString());
		// bw.close();
		//
		// log.debug("Done");
		//
		// } catch (IOException ex) {
		// ex.printStackTrace();
		// }
	}
}
