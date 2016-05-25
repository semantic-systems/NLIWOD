package org.aksw.hawk.number;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitController {
	private Map<String, IUnitLanguage> languageToHandler;
	private static Logger log = LoggerFactory.getLogger(UnitController.class);

	UnitController() {
		languageToHandler = new HashMap<>();
		/**
		 * How about moving all Languages to one package and instantiate all
		 * classes of this package here?
		 */
		languageToHandler.put("en", new UnitEnglish());
	}

	public String normalizeNumbers(final String langDescriptor, final String question) {
		if (languageToHandler.keySet().contains(langDescriptor)) {
			return languageToHandler.get(langDescriptor).convert(question);
		}
		log.debug("Failed to to normalize numbers and units - No implementation for given language");
		return question;
	}

	public Map<String, IUnitLanguage> getLanguageToHandler() {
		return languageToHandler;
	}

	public void setLanguageToHandler(final Map<String, IUnitLanguage> languageToHandler) {
		this.languageToHandler = languageToHandler;
	}

}
