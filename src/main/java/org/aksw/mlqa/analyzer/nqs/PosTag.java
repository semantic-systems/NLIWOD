package org.aksw.mlqa.analyzer.nqs;


import java.util.List;
import java.util.Properties;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j public class PosTag { 
	
	public PosTag(){}
	
	public String getTaggedSentence(String NLquery)
	{
		log.trace("tagging "+NLquery);
		Properties props = new Properties();
		props.setProperty("annotators","tokenize, ssplit, pos");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(NLquery);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		String taggedsentence = "";
		for (CoreMap sentence : sentences) {
			for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
				String word = token.get(CoreAnnotations.TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
				taggedsentence=taggedsentence+(word + "_" + pos+" ");
			}
		}
		log.debug("tagged sentence: "+ taggedsentence);
		return taggedsentence.trim();
	}
	
	/*		TAGS:

			CC Coordinating conjunction
			CD Cardinal number
			DT Determiner
			EX Existential there
			FW Foreign word
			IN Preposition or subordinating conjunction
			JJ Adjective
			JJR Adjective, comparative
			JJS Adjective, superlative
			LS List item marker
			MD Modal
			NN Noun, singular or mass
			NNS Noun, plural
			NNP Proper noun, singular
			NNPS Proper noun, plural
			PDT Predeterminer
			POS Possessive ending
			PRP Personal pronoun
			PRP$ Possessive pronoun
			RB Adverb
			RBR Adverb, comparative
			RBS Adverb, superlative
			RP Particle
			SYM Symbol
			TO to
			UH Interjection
			VB Verb, base form
			VBD Verb, past tense
			VBG Verb, gerund or present participle
			VBN Verb, past participle
			VBP Verb, non­3rd person singular present
			VBZ Verb, 3rd person singular present
			WDT Wh­determiner
			WP Wh­pronoun
			WP$ Possessive wh­pronoun
			WRB Wh­adverb

//			http://stackoverflow.com/questions/1833252/java-stanford-nlp-part-of-speech-labels
	 */
}