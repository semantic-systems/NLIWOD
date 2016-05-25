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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnitEnglish implements IUnitLanguage {
	private static Logger log = LoggerFactory.getLogger(UnitEnglish.class);
	HashMap<String, ImmutablePair<Double, String>> identifierToUnit;
	HashMap<String, Double> identifierToMultiplier;

	public UnitEnglish() {
		identifierToMultiplier = new HashMap<>();
		identifierToUnit = new HashMap<>();
		getIdentifierToUnit();
		System.out.println(identifierToUnit.toString());
	}

	@Override
	public String convert(final String q) {
		// TODO Auto-generated method stub
		return "";
	}

	private void getIdentifierToUnit() {
		final ClassLoader classLoader = getClass().getClassLoader();
		final File file = new File(classLoader.getResource("unitconversion/englishIdentifierToUnit.txt").getFile());
		List<List<String>> data = loadTabSplit(file);
		if (data == null || data.isEmpty()) {
			return;
		}

		for (List<String> line : data) {
			try {
				identifierToUnit.put(line.get(0), new ImmutablePair<>(new Double(line.get(1)), line.get(2)));
			} catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException e) {
				log.debug("Could not parse line " + data.indexOf(line) + "from file " + file.getAbsolutePath());
			}
		}

	}

	private List<List<String>> loadTabSplit(final File file) {
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

	public static void main(final String[] args) {
		new UnitEnglish();
	}

}
