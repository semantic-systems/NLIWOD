package org.aksw.hawk.experiment;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

//comment by Ricardo
//this looks promising although I am not convinced that it is useful

public class NatLogStanford {
	// http://stackoverflow.com/questions/32419534/stanford-openie-example-code-would-not-run-properly
	public static void main(String[] args) {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		List<HAWKQuestion> questions = null;

		questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD6_Train_Hybrid));

		for (Question q : questions) {
			String text = q.getLanguageToQuestion().get("en");
			System.out.println("\n" + text);
			Annotation doc = new Annotation(text);
			pipeline.annotate(doc);

			for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
				Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);
				for (RelationTriple triple : triples) {
					System.out.println(triple.confidence + "\t" + triple.subjectLemmaGloss() + "\t" + triple.relationLemmaGloss() + "\t" + triple.objectLemmaGloss());
				}
			}
		}

	}

}
