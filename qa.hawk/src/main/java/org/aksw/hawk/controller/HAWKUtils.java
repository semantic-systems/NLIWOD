package org.aksw.hawk.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.datastructure.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HAWKUtils {
	private static Logger log = LoggerFactory.getLogger(HAWKUtils.class);

	private HAWKUtils() {

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
			// proof if this label undercuts the last one.
			int currentNEStartPos = currentNE.getOffset();
			int currentNEEndPos = currentNEStartPos + currentNE.label.length();
			if (startFormerLabel >= currentNEEndPos) {
				textParts.add(sentence.substring(currentNEEndPos, startFormerLabel));
				textParts.add(currentNE.uris.get(0).getURI());
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

	// TODO @ christian einen test schreiben mit der frage : Who was vice
	// president under the president who approved the use of atomic weapons
	// against Japan during World War II?
	/**
	 * Replaces Named Entities in question string with corresponding URL, stored
	 * in {@link HAWKQuestion#getLanguageToNamedEntites()}
	 *
	 * @param q The HAWKQuestion to get data from.
	 * @return The question String with replaced Named Entities
	 */
	public static String replaceNamedEntitysWithURL(final HAWKQuestion q) {
		String sentence = q.getLanguageToQuestion().get("en");
		if (!q.getLanguageToNamedEntites().isEmpty()) {
			sentence = HAWKUtils.replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNamedEntites().get("en"));
			log.debug(sentence);
		}
		if (!q.getLanguageToNounPhrases().isEmpty()) {
			sentence = HAWKUtils.replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNounPhrases().get("en"));
			log.debug(sentence);
		}

		return sentence;
	}
}
