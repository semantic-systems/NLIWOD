package org.aksw.qa.commons.qald;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.qald.IQuestionCsvParser.Column.ColEnum;
import org.apache.jena.ext.com.google.common.base.Strings;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Convert IQuestions to CSVs, in both directions.
 *
 * @author jhuth
 */
public class IQuestionCsvParser {
	/**
	 * Some Columns can hold multiple values. So, a String representing this column content will be split on this char.
	 */
	public static String ARRAY_SPLIT = ",";
	/**
	 * The value that will be written if a column is declared for a csv but corresponding field in IQuestion is null
	 */
	public static String NULL_DEFAULT = "";

	/**
	 * Works as described in {@link #csvToQuestionList(CSVReader, Column...)}
	 *
	 * @param csvRow
	 *            A row of a csv, with decoded terminators and split up
	 * @param columns
	 * @return
	 * @throws IOException
	 */
	private static IQuestion csvRowToQuestion(final String[] csvRow, final Column... columns) throws IOException {
		List<String> splittedcsv = Arrays.asList(csvRow);
		List<Column> cols = Arrays.asList(columns);

		int maxFor = cols.size() < splittedcsv.size() ? cols.size() : splittedcsv.size();

		IQuestion question = new Question();

		for (int i = 0; i < maxFor; i++) {
			Column colIt = cols.get(i);
			String csvIt = splittedcsv.get(i).trim();

			ColEnum en = colIt.getEnum();
			String lang = colIt.getState();
			try {

				switch (en) {
				case AGGREGATION_FLAG:
					question.setAggregation(parseBoolean(csvIt));
					break;
				case ANSWER_TYPE:
					question.setAnswerType(csvIt.toLowerCase());
					break;
				case GOLDEN_ANSWERS:

					question.getGoldenAnswers().addAll(parseStringArray(csvIt));
					break;
				case HYBRID_FLAG:
					question.setHybrid(parseBoolean(csvIt));
					break;
				case KEYWORDS:

					List<String> keywords = parseStringArray(csvIt);

					if (!question.getLanguageToKeywords().keySet().contains(lang)) {
						question.getLanguageToKeywords().put(lang, keywords);
					} else {
						question.getLanguageToKeywords().get(lang).addAll(keywords);
					}

					break;
				case ONLYDBO_FLAG:
					question.setOnlydbo(parseBoolean(csvIt));
					break;
				case OUT_OF_SCOPE_FLAG:
					question.setOutOfScope(parseBoolean(csvIt));
					break;
				case PSEUDO_SPARQL_QUERY:
					question.setPseudoSparqlQuery(csvIt);
					break;
				case QUESTION:
					question.getLanguageToQuestion().put(lang, csvIt);
					break;
				case SPARQL_QUERY:
					question.setSparqlQuery(csvIt);
					break;
				case __IGNORE:
					continue;
				default:
					throw new ParseException("Unidentified column wrapper", 0);

				}
			} catch (Exception e) {
				throw new IOException(String.format("@Column %d : |%s| should be parsed as: %s", i, csvIt, colIt.getEnum().name()), e);
			}
		}

		return question;

	}

	/**
	 * Reads a CSV which source is defined by given {@link CSVReader}, which also defines the separation and quotation chars. {@link CSVReader} can also be set up to skip the first n lines.
	 * <p>
	 * Define the Column structure if the CSV by passig {@link Column} Objects. The order has to fit the column order of the csv.
	 * <p>
	 * For example, following call: <blockquote> csvToQuestionList(reader,<b>{@link Column.ID(), Column.ignore(), Column.question("en") ) } </b> </blockquote>
	 * <p>
	 * Will parse the first columnn of the csv to {@link IQuestion#setId()} , ignore the second column, and will parse column 3 as question in english.
	 * <p>
	 * Trailing csv columns (no {@link Column } present) will be ignored, also, additionally defined {@link Column}s will be ignored.
	 * <p>
	 * {@link Column}s can appear multiple times. For a column with one value(e.g. a boolean flag, sparql query) the rightmost column is used. For Columns holding possible array contents (e.g. golden
	 * answers, keywords), all defined columns will be parsed.
	 * <p>
	 * Some columns can hold arrays, e.g. {@link Column#goldenAnswers()}, {@link Column#keywords(String)}. The array separator string can be set via {@link #ARRAY_SPLIT} Arrays can be preceeded and
	 * concluded by square and curly brackets. So, a toString on most Collections would be a valid input format.
	 *
	 * @param reader
	 *            A CSVReader fitting to your needs. For GoogleDocs CSV export, you can use {@link #readerForGoogleDocsCsvExports(Reader, int)}
	 * @param columns
	 *            as described above
	 * @return A list of IQuestions with the parseable information set.
	 * @throws IOException
	 *             if something bad happens
	 */
	public static List<IQuestion> csvToQuestionList(final CSVReader reader, final Column... columns) throws IOException {

		List<IQuestion> questions = new Vector<>();
		String[] row = reader.readNext();
		int lineCounter = 1;

		while (row != null) {
			try {
				questions.add(csvRowToQuestion(row, columns));
			} catch (IOException e) {
				throw new IOException(String.format("Couldn't parse csv: @Line %s " + e.getMessage(), lineCounter), e.getCause());
			}

			row = reader.readNext();
			lineCounter++;

		}
		return questions;
	}

	/**
	 * Writes all data specified by the columns from all given IQuestions to the csvwriter. Get more information from the doc of {@link #csvToQuestionList(CSVReader, Column...)}
	 * <p>
	 * If or a declared {@link Column} no information in IQuestion is present(e.g. field is null), {@link #NULL_DEFAULT} will be written.
	 *
	 * @param writer
	 * @param columnDescriptorgRow
	 *            - the first row will contain the names of the columns
	 * @param questions
	 * @param columns
	 * @throws IOException
	 */
	public static void questionListToCsv(final CSVWriter writer, final boolean columnDescriptorgRow, final List<IQuestion> questions, final Column... columns) throws IOException {

		if (columnDescriptorgRow) {
			int index = 0;
			String[] names = new String[columns.length];
			for (Column it : columns) {
				names[index] = it.getEnum().name();
				if (it.getState() != null) {
					names[index] += String.format(" [%s]", it.getState());
				}
				index++;
			}
			writer.writeNext(names);
		}

		for (IQuestion it : questions) {
			ArrayList<String> colString = new ArrayList<>(columns.length);
			for (Column colIt : Arrays.asList(columns)) {
				ColEnum en = colIt.getEnum();

				switch (en) {
				case AGGREGATION_FLAG:
					colString.add(Objects.toString(it.getAggregation(), NULL_DEFAULT));
					break;
				case ANSWER_TYPE:
					colString.add(Objects.toString(it.getAnswerType(), NULL_DEFAULT));
					break;
				case GOLDEN_ANSWERS:
					colString.add(Objects.toString(it.getGoldenAnswers(), NULL_DEFAULT));
					break;
				case HYBRID_FLAG:
					colString.add(Objects.toString(it.getHybrid(), NULL_DEFAULT));
					break;
				case ID:
					colString.add(Objects.toString(it.getId(), NULL_DEFAULT));
					break;
				case KEYWORDS:
					colString.add(Objects.toString(it.getLanguageToKeywords().get(colIt.getState()), NULL_DEFAULT));
					break;
				case ONLYDBO_FLAG:
					colString.add(Objects.toString(it.getOnlydbo(), NULL_DEFAULT));
					break;
				case OUT_OF_SCOPE_FLAG:
					colString.add(Objects.toString(it.getOutOfScope(), NULL_DEFAULT));
					break;
				case PSEUDO_SPARQL_QUERY:
					colString.add(Objects.toString(it.getPseudoSparqlQuery(), NULL_DEFAULT));
					break;
				case QUESTION:
					colString.add(Objects.toString(it.getLanguageToQuestion().get(colIt.getState()), NULL_DEFAULT));
					break;
				case SPARQL_QUERY:
					colString.add(Objects.toString(it.getSparqlQuery(), NULL_DEFAULT));
					break;
				case __IGNORE:
					colString.add(NULL_DEFAULT);
					break;
				default:
					throw new IOException("No case statement for enum.");

				}

			} //end colFor
			String[] ls = new String[colString.size()];
			colString.toArray(ls);
			writer.writeNext(ls);
		} //end questionFor

	}

	/**
	 * Parses a boolean. Difference to {@link Boolean#parseBoolean(String)} is that no other values than {"true","false"} are accepted, ignoring case.
	 *
	 * @param boolStr
	 * @return a parsed boolean
	 * @throws IOException
	 *             if boolStr not in {"true","false"}, ignoring case
	 */
	private static Boolean parseBoolean(final String boolStr) throws IOException {
		if (Strings.isNullOrEmpty(boolStr)) {
			throw new IOException(String.format("Coluldn't parse Boolean :|%s| empy or null not allowed.", boolStr));
		}
		String cleaned = boolStr.trim().toLowerCase();
		if (cleaned.matches(Boolean.TRUE.toString()) || cleaned.matches(Boolean.FALSE.toString())) {
			return Boolean.parseBoolean(cleaned);
		}
		throw new IOException(String.format("Coluldn't parse Boolean :|%s| is not valid.", boolStr));

	}

	/**
	 * Splits a string on {@link #ARRAY_SPLIT}. First and last char can be square or curly brackets, they will be removed
	 *
	 * @param arrayStr
	 * @return
	 */
	private static List<String> parseStringArray(final String arrayStr) {
		String cleaned = arrayStr.trim();

		if (cleaned.startsWith("[") || cleaned.startsWith("{")) {
			cleaned = cleaned.substring(1);
		}
		if (cleaned.endsWith("]") || cleaned.endsWith("}")) {
			cleaned = cleaned.substring(0, cleaned.length() - 1);
		}
		return Arrays.asList(cleaned.split(ARRAY_SPLIT));

	}

	/**
	 * Wrapper for enum to make different enum states possible;
	 *
	 * @see IQuestionCsvParser#csvToQuestionList(CSVReader, Column...)
	 */
	static class Column {
		private ColEnum en;
		private String state;

		private Column(final ColEnum en, final String state) {
			this.en = en;
			this.state = state;
		}

		protected ColEnum getEnum() {
			return en;
		}

		protected String getState() {
			return state;
		}

		protected Column(final ColEnum en) {
			this(en, null);
		}

		protected static Column ignore() {
			return new Column(ColEnum.__IGNORE);
		}

		protected static Column iD() {
			return new Column(ColEnum.ID);
		}

		protected static Column aggregationFlag() {
			return new Column(ColEnum.AGGREGATION_FLAG);
		}

		protected static Column onlyDboFlag() {
			return new Column(ColEnum.ONLYDBO_FLAG);
		}

		protected static Column outOfScopeFlag() {
			return new Column(ColEnum.OUT_OF_SCOPE_FLAG);
		}

		protected static Column hybridFlag() {
			return new Column(ColEnum.HYBRID_FLAG);
		}

		protected static Column sparqlQuery() {
			return new Column(ColEnum.SPARQL_QUERY);
		}

		protected static Column pseudoSparqlQuery() {
			return new Column(ColEnum.PSEUDO_SPARQL_QUERY);
		}

		protected static Column answerType() {
			return new Column(ColEnum.ANSWER_TYPE);
		}

		protected static Column goldenAnswers() {
			return new Column(ColEnum.GOLDEN_ANSWERS);
		}

		protected static Column question(final String lang) {
			return new Column(ColEnum.QUESTION, lang);
		}

		protected static Column keywords(final String lang) {
			return new Column(ColEnum.KEYWORDS, lang);
		}

		/**
		 * enum relating to the possible fields of {@link IQuestion}
		 */
		protected enum ColEnum {
								__IGNORE,
								ID,
								AGGREGATION_FLAG,
								ONLYDBO_FLAG,
								OUT_OF_SCOPE_FLAG,
								HYBRID_FLAG,
								SPARQL_QUERY,
								PSEUDO_SPARQL_QUERY,
								ANSWER_TYPE,
								GOLDEN_ANSWERS,
								QUESTION,
								KEYWORDS;
		}

	}

	/**
	 * Returns reader which uses comma(,) as separator and (") as quote. Sufficient to read data which was generated with googleDocs csv-export.
	 *
	 * @param reader
	 * @param skipLines
	 *            skip the first n lines
	 * @return
	 */
	public static CSVReader readerForGoogleDocsCsvExports(final Reader reader, final int skipLines) {
		return new CSVReader(reader, ',', '"', skipLines);
	}
}
