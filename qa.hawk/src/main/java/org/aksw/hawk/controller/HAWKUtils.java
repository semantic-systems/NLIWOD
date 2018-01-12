package org.aksw.hawk.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arq.qexpr;

public class HAWKUtils {
	private static Logger log = LoggerFactory.getLogger(HAWKUtils.class);

	private HAWKUtils() {

	}
	/**
	 *
	 * @param sentence Sentence to transform
	 * @param replace String to replace
	 * @param replacement desired string
	 * @return Sentence with replaced labels
	 * @author rricha
	 */
	
	public static String replaceLabelByAnnotation(final String sentence, final String replace, final String replacement) {
		StringBuilder result = new StringBuilder();
		String delimiters = "? ";
	    StringTokenizer st = new StringTokenizer(sentence, delimiters, true);
	    while (st.hasMoreTokens()) {
	        String w = st.nextToken();
	        if (w.equals(replace) || w.contains(replace) && !w.startsWith("http")) {
	            result.append(replacement);
	        } else {
	            result.append(w);
	        }
	    }
		return result.toString();
	}

	/**
	 *
	 * @param Sentence Sentence to transform
	 * @param list List of Entities to replace labels with
	 * @return Sentence with replaced labels
	 */
	public static String replaceLabelsByIdentifiedURIs(final String sentence, final List<Entity> list) {
		/*
		 * reverse list of entities to start replacing from the end of the
		 * string so that replacing from the end won't mess up the order
		 */
		List<String> textParts = new ArrayList<>();

		list.sort(Comparator.comparing(Entity::getOffset).reversed());
		
		int startFormerLabel = sentence.length();
		for (Entity currentNE : list) {
			if (sentence.contains(currentNE.getUris().get(0).getURI())) 
				break;
			
			int currentNEStartPos = currentNE.getOffset();
			
			int currentNEEndPos = currentNEStartPos + currentNE.getLabel().length();
			
			if (startFormerLabel >= currentNEEndPos) {
				textParts.add(sentence.substring(currentNEEndPos, startFormerLabel));
				textParts.add(currentNE.getUris().get(0).getURI());
				startFormerLabel = currentNEStartPos;
			}
		}
		if (startFormerLabel > 0) {
			textParts.add(sentence.substring(0, startFormerLabel));
		}
		StringBuilder textWithMarkups = new StringBuilder();
		for (int i = textParts.size() - 1; i >= 0; --i) {
			textWithMarkups.append(textParts.get(i));
		}
		return textWithMarkups.toString();
		
	}
	
	public static String replaceCombinedNounsWithURL(final String sentence, final List<Entity> list) {
		
		int startFormerLabel = sentence.length();
		List<String> textParts = new ArrayList<>();
		
		for (Entity currentCNN : list) {
			if (sentence.contains(currentCNN.getUris().get(0).getURI())) 
				break;
			int offset = currentCNN.getOffset();
			int currentCNNStartPos = StringUtils.ordinalIndexOf(sentence, " ", offset-1) + 1;
			int currentCNNEndPos = currentCNNStartPos + currentCNN.getLabel().length();
			if (startFormerLabel >= currentCNNEndPos) {
				textParts.add(sentence.substring(currentCNNEndPos, startFormerLabel));
				textParts.add(currentCNN.getUris().get(0).getURI());
				startFormerLabel = currentCNNStartPos;
			}
		}
		if (startFormerLabel > 0) {
			textParts.add(sentence.substring(0, startFormerLabel));
		}
		StringBuilder textWithMarkups = new StringBuilder();
		for (int i = textParts.size() - 1; i >= 0; --i) {
			textWithMarkups.append(textParts.get(i));
		}
		return textWithMarkups.toString();
	
	}

	/**
	 * Replaces Named Entities in question string with corresponding URL, stored
	 * in {@link HAWKQuestion#getLanguageToNamedEntites()}
	 *
	 * @param q The HAWKQuestion to get data from.
	 * @return The question String with replaced Named Entities
	 */
	public static String replaceNamedEntitysWithURL(final HAWKQuestion q) {
		String sentence = q.getTransformedQuestion();
		
		if (!q.getLanguageToNamedEntites().isEmpty() && !q.getLanguageToNamedEntites().get("en").isEmpty()) {
			sentence = HAWKUtils.replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNamedEntites().get("en"));
			log.debug(sentence);
		}
		if (!q.getLanguageToNounPhrases().isEmpty() && !q.getLanguageToNounPhrases().get("en").isEmpty()) {
			sentence = HAWKUtils.replaceCombinedNounsWithURL(sentence, q.getLanguageToNounPhrases().get("en"));
			log.debug(sentence);
		}
		q.setTransformedQuestion(sentence);
		return sentence;
	}
}
