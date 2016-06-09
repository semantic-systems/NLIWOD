package org.aksw.hawk.number;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
	 * @param file The file you want to load.
	 * @return A List containing lists. Outer list are the lines, inner lists
	 *         are the values separated by tab.
	 */
	public static List<List<String>> loadTabSplit(final File file) {
		List<List<String>> ret = new ArrayList<>();

		FileReader fileReader;
		try {
			fileReader = new FileReader(file);
		} catch (FileNotFoundException e) {
			log.debug("Could not load number conversion rules - File not fond" + file.getAbsolutePath(), e);
			return null;
		}
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		try {
			String line = bufferedReader.readLine();
			while (line != null) {
				if (line.startsWith("//")) {
					line = bufferedReader.readLine();
					continue;
				}
				ret.add(new ArrayList<>(Arrays.asList(line.split("\t"))));
				line = bufferedReader.readLine();
			}

		} catch (IOException e) {
			log.debug("Error while parsing number conversion rules " + file.getAbsolutePath(), e);
		}
		try {
			bufferedReader.close();
		} catch (IOException e) {
			log.debug("Could not close resource " + file.getAbsolutePath(), e);
		}
		return ret;
	}

}
