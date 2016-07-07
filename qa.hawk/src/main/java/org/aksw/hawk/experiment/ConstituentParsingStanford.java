package org.aksw.hawk.experiment;

import java.util.List;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Constituent;
import edu.stanford.nlp.trees.LabeledScoredConstituent;
import edu.stanford.nlp.trees.Tree;

public class ConstituentParsingStanford {
	// TODO after transforming to classes, extent NounphraseIdentification class
	// and benchmark their behaviour in hybrid scenario
	public static void main(String[] args) {

		List<HAWKQuestion> questions = null;

		questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD6_Train_Hybrid));
		// TODO transform both loops to nounphrase extractor classes

		// TODO it could be helpful to also run NER and excluded recognized
		// entities

		// TODO remove overlapping NP
		for (Question q : questions) {
			String text = q.getLanguageToQuestion().get("en");
			System.out.println("\n" + text);
			Document doc = new Document(text);
			for (Sentence sent : doc.sentences()) {
				for (Tree tree : sent.parse().subTreeList()) {
					if (tree.isPhrasal()) {
						if (tree.label().value().equals("NP")) {
							if (tree.children().length > 1) {
								// check that subtree does not contain a PP or a
								// VP
								boolean containsPPorVP = false;
								for (Tree subtree : tree.subTreeList()) {
									if (subtree.isPhrasal() && subtree.label().value().matches("PP||VP")) {
										containsPPorVP = true;
										break;
									}
								}
								if (!containsPPorVP) {
									System.out.println(tree.toString());
								}
							}
						}
					}
				}
			}
		}

		// TODO this can definitely used as noun phrase extracted
		for (Question q : questions) {
			String text = q.getLanguageToQuestion().get("en");
			System.out.println("\n" + text);
			Document doc = new Document(text);
			for (Sentence sent : doc.sentences()) {
				for (String keyphrase : sent.algorithms().keyphrases()) {
					System.out.println(keyphrase);
				}
			}
		}

	}
}
