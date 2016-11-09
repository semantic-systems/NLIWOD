package org.aksw.hawk.number;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitController {
	private Map<String, IUnitLanguage> languageToHandler;
	private static Logger log = LoggerFactory.getLogger(UnitController.class);

	public UnitController() {
		languageToHandler = new HashMap<>();
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

	public void instantiateEnglish(final StanfordNLPConnector stanford) {
		languageToHandler.put("en", new UnitEnglish(stanford));
	}

	/**
	 * Loads a file with tab separated values. Lines starting with "//" will be
	 * ignored.
	 * 
	 * @param input
	 *            The file you want to load.
	 * @return A List containing lists. Outer list are the lines, inner lists
	 *         are the values separated by tab.
	 * @throws IOException 
	 */
	public static List<List<String>> loadTabSplit(final InputStream input) throws IOException {
		List<List<String>> ret = new ArrayList<>();

		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
		String line = bufferedReader.readLine();
		while (line != null) {
			if (line.startsWith("//")) {
				line = bufferedReader.readLine();
				continue;
			}
			ret.add(new ArrayList<>(Arrays.asList(line.split("\t"))));
			line = bufferedReader.readLine();
		}

		bufferedReader.close();
		return ret;
	}

}
