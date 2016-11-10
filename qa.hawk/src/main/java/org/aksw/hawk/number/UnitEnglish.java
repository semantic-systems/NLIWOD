package org.aksw.hawk.number;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.aksw.hawk.controller.StanfordNLPConnector;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class converts natural word numbers to digit numbers and, if a unit is
 * given, converts this unit to a base unit specified in a resource. The input
 * can be any string and can have multiple occurrences of numbers and units.
 *
 * <pre>
 * Examples:
 * "Give me two hundred birds" -> "Give me 200 birds"
 * "$80 thousand and three hundred four" -> "$ 80304"
 * "Show the first ten screws with a length of two inches" -> "Show the first 10 screws with a length of 0.0508 m"
 * "10 miles" -> "1609.344 m"
 * 
 * "one" can be a numeral, but it does not have to be. To handle those cases,
 * The Stanford pipeline will be started on sentences containing "one". It will only be converted to a number,
 * if its not in a relation "nmod" with any other word. With this, we can handle sentences like:
 * 
 * "The color is different from the old one."
 * 
 *  This sentence will be the same after parsing.
 *
 * </pre>
 *
 */
public class UnitEnglish implements IUnitLanguage {
	private static Logger log = LoggerFactory.getLogger(UnitEnglish.class);
	HashMap<String, ImmutablePair<Double, String>> identifierToUnit;
	HashMap<String, Double> identifierToMultiplier;
	StanfordNLPConnector stanford;

	/**
	 * Stanford is needed to handle sentences containing "one"
	 *
	 */
	public UnitEnglish(final StanfordNLPConnector stanford) {
		this.stanford = stanford;
		identifierToMultiplier = new HashMap<>();
		identifierToUnit = new HashMap<>();
		try {
	        loadResource();
        } catch (IOException e) {
	        e.printStackTrace();
        }
	}

	@Override
	public String convert(final String q) {
		String out = "";
		if ((q == null) || q.isEmpty()) {
			return out;
		}
		/**
		 * check if "one" is in sentence. If so, parse sentence with stanford.
		 * Get all parts annotated as number by stanford NER. If those numbers
		 * are annotated with "nmod", dont convert it.
		 */
		if (q.toLowerCase().matches("(.*)(\\s)(one)([\\p{Punct}\\s])(.*)")) {
			Annotation document = stanford.runAnnotation(q);
			List<CoreMap> sentences = document.get(SentencesAnnotation.class);
			CoreMap sentenc = sentences.get(0);
			SemanticGraph dependencies = sentenc.get(CollapsedCCProcessedDependenciesAnnotation.class);
			ArrayList<String> parseForNumber = new ArrayList<>();
			ArrayList<String> outArray = new ArrayList<>();
			boolean lastWasNumber = false;
			boolean numberContainsNmod = false;
			for (CoreLabel token : sentenc.get(TokensAnnotation.class)) {
				String ner = token.get(NamedEntityTagAnnotation.class);
				if (ner.toLowerCase().matches("number|duration|currency")) {
					lastWasNumber = true;
					IndexedWord depWord = dependencies.getNodeByIndex(token.index());
					List<SemanticGraphEdge> depEdges = dependencies.getIncomingEdgesSorted(depWord);
					numberContainsNmod = false;
					for (SemanticGraphEdge it : depEdges) {
						if (it.getRelation().getShortName().toLowerCase().equals("nmod")) {
							numberContainsNmod = true;
							break;
						}
					}
					outArray.add(token.word());
				} else {
					if (lastWasNumber && !numberContainsNmod) {

						outArray.add(convertToBaseUnit(replaceNumerals(Joiner.on(" ").join(parseForNumber))));
						outArray.add(token.word());
						parseForNumber = new ArrayList<>();
					} else {
						outArray.addAll(parseForNumber);
						outArray.add(token.word());
					}
					lastWasNumber = false;

				}

			}
			if (lastWasNumber && numberContainsNmod) {
				outArray.addAll(parseForNumber);
			}
			if (lastWasNumber && !numberContainsNmod) {
				outArray.add(replaceNumerals(Joiner.on(" ").join(parseForNumber)));

			}
			return Joiner.on(" ").join(outArray).replaceAll("(\\s+)(\\p{Punct})(\\s*)$", "$2");

		} else {
			/**
			 * if "one" is not in this sentence, straight forward number and
			 * base unit conversion. (order matters)
			 */
			return convertToBaseUnit(replaceNumerals(q)).trim();

		}

	}

	/**
	 * Converts occurring natural language numerals to digits in any given
	 * String.
	 *
	 * For examples: {@link UnitEnglish}
	 *
	 * This only works when natural language numerals are stored in
	 * identifierToMultiplier. Call {@link UnitEnglish#loadTabSplit(File)}
	 * beforehand, to load data. To add numerals which should be recognized,
	 * edit resource file.
	 *
	 * @param replaceThis
	 *            String which may or may not contain something to convert.
	 * @return InputString, but with replaced natural language numerals.
	 */
	private String replaceNumerals(final String replaceThis) {
		if ((replaceThis == null) || replaceThis.isEmpty()) {
			return "";
		}
		log.debug("Replacing numerals on :" + replaceThis);
		String out = "";
		Double val = 1.0;
		Double lastNumber = 0.0;
		/**
		 * Inserting whitespace before punctuation.
		 */
		String withWhitespace = this.insertWhitespacebeforePunctuation(replaceThis);
		/**
		 * Inserting whitespace between currency denominator and value ("$80" ->
		 * "$ 80")
		 */
		withWhitespace = withWhitespace.replaceAll("(\\p{Sc})(\\d+)", "$1 $2");
		ArrayList<String> split = new ArrayList<>(Arrays.asList(withWhitespace.split(" ")));

		ArrayList<String> cleaned = split;
		// Removed, its more likely to be a named entity than a numeral.
		// for cases like twenty-two
		// for (String it : split) {
		// cleaned.addAll(Arrays.asList(it.split("-")));
		// }
		boolean lastWasNumber = false;
		for (int i = 0; i < cleaned.size(); i++) {
			String it = cleaned.get(i);
			Double parsedNumber = parseWord(it);
			/**
			 * Not in a number and no number read-> normal word
			 */
			if ((parsedNumber == null) && !lastWasNumber) {
				out += it + " ";
				continue;
			}
			/**
			 * End of a number reached
			 */
			if ((parsedNumber == null) && lastWasNumber) {

				/**
				 * Ignore "and" in cases "hundred and five"
				 */
				if (it.equals("and")) {
					continue;
				}
				/**
				 * If its not "and", add cast number to output string. Only with
				 * decimal point, when necessary.
				 */
				lastWasNumber = false;

				out = prettyAppendDouble(out, val) + it + " ";

				continue;
			}
			/**
			 * Iterator is a number
			 */
			if (parsedNumber != null) {
				/**
				 * Calculate val
				 */
				if (lastWasNumber) {

					if (parsedNumber < lastNumber) {
						val += parsedNumber;
					} else {
						val *= parsedNumber;
					}
					lastNumber = parsedNumber;

				} else {
					val = parsedNumber;
					lastNumber = parsedNumber;
					lastWasNumber = true;
				}
				/**
				 * If this number is also last string in list, append already
				 * generated value to output.
				 */
				if (i == (cleaned.size() - 1)) {
					out = prettyAppendDouble(out, val);
				}

			}

		}

		return out.trim();
	}

	/**
	 * Converts occurring units to their base unit. Iterates over given string.
	 * if a unit is found, and the word left from unit is a number, conversion
	 * takes place.
	 *
	 * @param str
	 *            Any string which may or may not contain a unit
	 * @return Input string but with unit converted to base unit.
	 */
	private String convertToBaseUnit(final String str) {
		String out = "";
		if ((str == null) || str.isEmpty()) {
			return out;
		}
		log.debug("converting base units for: " + str);
		String withWhitespace = this.insertWhitespacebeforePunctuation(str);
		ArrayList<String> split = new ArrayList<>(Arrays.asList(withWhitespace.split(" ")));
		Double lastIt = null;
		for (String it : split) {

			if (lastIt != null) {
				if (identifierToUnit.containsKey(it)) {
					out += (lastIt * identifierToUnit.get(it).getLeft()) + " " + identifierToUnit.get(it).getRight() + " ";
				} else {
					out = prettyAppendDouble(out, lastIt) + it + " ";
				}
				lastIt = null;
			} else {
				try {
					lastIt = Double.parseDouble(it);

				} catch (NumberFormatException e) {
					out += it + " ";
				}
			}

		}
		if (lastIt != null) {
			out = prettyAppendDouble(out, lastIt);
		}

		return out.replaceAll("(\\s+)(\\p{Punct})(\\s*)$", "$2");

	}

	private String insertWhitespacebeforePunctuation(final String input) {
		return input.replaceAll("(\\w)([\\.?!])(\\s*)$", "$1 $2");
	}

	/**
	 * Appends a String representation of a Double to input string. If its
	 * parseable to an integer, decimals will not be printed. All zeros will be
	 * printed. Scientific representation will not be used.
	 *
	 * @param out
	 *            String to append to.
	 * @param val
	 *            Val to append.
	 * @return String with appended Double val.
	 */
	private String prettyAppendDouble(final String out, final Double val) {
		if ((val == Math.floor(val)) && !Double.isInfinite(val)) {
			return out + val.intValue() + " ";

		} else {
			DecimalFormat df = new DecimalFormat("#");
			return out + df.format(val);
		}
	}

	/**
	 *
	 * @param s
	 *            String to be parsed to Double. Also accepts all numeral words
	 *            defined in English file
	 * @return If String is parsable to Double, this will be returned. otherwise
	 *         null.
	 */
	private Double parseWord(final String s) {
		Double out = null;
		try {
			out = Double.parseDouble(s);
		} catch (NumberFormatException e) {
			out = identifierToMultiplier.get(s);
		}
		return out;
	}

	private void loadResource() throws IOException {
		log.debug("Loading number conversion rules for english");
		
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream input = classLoader.getResourceAsStream("unitconversion/englishIdentifierToUnit.txt");

		List<List<String>> data = UnitController.loadTabSplit(input);
		if ((data == null) || data.isEmpty()) {
			return;
		}
		for (List<String> line : data) {
			try {
				if (line.size() == 3) {
					identifierToUnit.put(line.get(0), new ImmutablePair<>(new Double(line.get(1)), line.get(2)));
				} else {
					identifierToMultiplier.put(line.get(0), new Double(line.get(1)));
				}

			} catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException e) {
				log.debug("Could not parse line " + data.indexOf(line) + "from file " +input.toString());
			}
		}
	}

	public static void main(final String[] args) {
		UnitEnglish ue = new UnitEnglish(null);
		String q = "$80 thousand and three hundred four";
		log.debug("Start conversion");
		System.out.println(ue.convert(q));
		/*-
		HashMap<String, String> sentenceToSentence = new HashMap<>();
		sentenceToSentence.put("Which countries have more than ten volcanoes?", "Which countries have more than 10 volcanoes?");
		sentenceToSentence.put("What are the five boroughs of New York?", "What are the 5 boroughs of New York?");
		sentenceToSentence.put("Which presidents of the United States had more than three children?", "Which presidents of the United States had more than 3 children?");
		sentenceToSentence.put("Which locations have more than two caves?", "Which locations have more than 2 caves?");
		sentenceToSentence.put("Which cities have more than 2 million inhabitants?", "Which cities have more than 2000000 inhabitants?");
		sentenceToSentence.put("Give me all world heritage sites designated within the past five years.", "Give me all world heritage sites designated within the past 5 years.");
		sentenceToSentence.put("Does the new Battlestar Galactica series have more episodes than the old one?", "Does the new Battlestar Galactica series have more episodes than the old one?");
		sentenceToSentence.put("Which city does the first person to climb all 14 eight-thousanders come from?", "Which city does the first person to climb all 14 eight-thousanders come from?");
		sentenceToSentence.put("Give me all films produced by Steven Spielberg with a budget of at least $80 million.",
		       "Give me all films produced by Steven Spielberg with a budget of at least $ 80000000.");
		sentenceToSentence.put("List the seven kings of Rome.", "List the 7 kings of Rome.");

		for (String q : sentenceToSentence.keySet()) {
		System.out.println(ue.convert(q));
		}
		 */

	}

}
