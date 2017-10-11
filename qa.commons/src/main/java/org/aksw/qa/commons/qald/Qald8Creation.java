package org.aksw.qa.commons.qald;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.load.json.EJDataset;
import org.aksw.qa.commons.load.json.EJQuestionFactory;
import org.aksw.qa.commons.load.json.ExtendedQALDJSONLoader;
import org.aksw.qa.commons.load.json.QaldJson;
import org.aksw.qa.commons.qald.IQuestionCsvParser.Column;
import org.aksw.qa.commons.sparql.AnswerSyncer;
import org.aksw.qa.commons.sparql.SPARQL;
import org.aksw.qa.commons.sparql.SPARQLEndpoints;
import org.aksw.qa.commons.sparql.SPARQLPrefixResolver;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class Qald8Creation {

	public static void main(final String[] args) throws Exception {

		qald8test();
		qald8train();

	}

	public static void qald8train() throws Exception {
		List<IQuestion> questions = LoaderController.load(Dataset.QALD7_Train_Multilingual);

		List<IQuestion> questions7test = LoaderController.load(Dataset.QALD7_Test_Multilingual);

		//Give test questions different id range, for debugging
		for (IQuestion q : questions7test) {
			q.setId((Integer.parseInt(q.getId()) + 300) + "");
		}

		questions.addAll(questions7test);
		List<IQuestion> failQuestions = new Vector<>();

		List<String> skipId = Arrays.asList("15", "30", "43", "57", "62", "90", "94", "124", "199", "213");

		List<IQuestion> removeQuestions = new Vector<>();

		/**
		 * Remove handpicked queries
		 */
		for (IQuestion it : questions) {
			if (skipId.contains(it.getId())) {
				removeQuestions.add(it);

			}

		}

		questions.removeAll(removeQuestions);
		failQuestions.addAll(removeQuestions);

		removeQuestions.clear();
		/**
		 * Add missing sparql prefixes
		 */
		for (IQuestion it : questions) {
			String oldQuery = it.getSparqlQuery();
			String newSparql = SPARQLPrefixResolver.addMissingPrefixes(it.getSparqlQuery());
			it.setSparqlQuery(newSparql);
		}
		/**
		 * Filter questions with invalid sparqls.
		 */
		for (IQuestion it : questions) {
			if (!SPARQL.isValidSparqlQuery(it.getSparqlQuery())) {
				removeQuestions.add(it);
			}
		}
		questions.removeAll(removeQuestions);
		failQuestions.addAll(removeQuestions);

		removeQuestions.clear();

		/**
		 * Retrieve newest answerset from server.
		 */

		for (IQuestion it : questions) {
			try {
				AnswerSyncer.syncAnswers(it, SPARQLEndpoints.DBPEDIA_ORG);
			} catch (Exception e) {
				removeQuestions.add(it);
			}
		}
		questions.removeAll(removeQuestions);
		failQuestions.addAll(removeQuestions);

		removeQuestions.clear();

		/**
		 * Filter questions which have no answer.
		 */
		for (IQuestion it : questions) {
			if (it.getGoldenAnswers().isEmpty()) {
				removeQuestions.add(it);
			}
		}

		questions.removeAll(removeQuestions);
		failQuestions.addAll(removeQuestions);

		removeQuestions.clear();

		/**
		 * Final step: new IDs
		 */
		int newID = 1;
		for (IQuestion q : questions) {
			q.setId("" + newID++);
		}

		/**
		 * Write the json
		 */

		File parentDir = new File("c:/output/qald8/");

		File qaldjsonFile = new File(parentDir, "qald-8-train-multilingual.json ");
		qaldjsonFile.delete();

		QaldJson qaldJson = EJQuestionFactory.getQaldJson(questions);

		EJDataset header = new EJDataset();
		header.setId("qald-8-train-multilingual");
		qaldJson.setDataset(header);

		ExtendedQALDJSONLoader.writeJson(qaldJson, qaldjsonFile, true);

		/**
		 * Write debug files / overview about good questions
		 */
		File f = new File(parentDir, "qald8trainGOOD_QUESTIONS.csv ");
		f.delete();
		FileWriter fw = new FileWriter(f);

		CSVWriter wr = new CSVWriter(fw, ';', '"');

		IQuestionCsvParser.questionListToCsv(wr, true, questions, Column.ID(), Column.question("en"), Column.sparqlQuery(), Column.goldenAnswers());
		wr.flush();
		fw.close();

		/**
		 * Write debug files / all dismissed questions
		 */
		File f2 = new File(parentDir, "qald8trainBAD_QUESTIONS.csv ");
		f2.delete();
		FileWriter fw2 = new FileWriter(f2);

		CSVWriter wr2 = new CSVWriter(fw2, ';', '"');

		IQuestionCsvParser.questionListToCsv(wr2, true, failQuestions, Column.ID(), Column.question("en"), Column.sparqlQuery(), Column.goldenAnswers());
		wr2.flush();
		fw2.close();
		System.out.println("done qald8train");
	}

	public static void qald8test() throws Exception {
		/**
		 * csv export from googledocs
		 */
		FileReader rd = new FileReader(new File("c:/output/QALD8testtask1.csv"));

		File parentDir = new File("c:/output/qald8/");

		CSVReader csvReader = IQuestionCsvParser.readerForGoogleDocsCsvExports(rd, 1);

		List<IQuestion> que = IQuestionCsvParser.csvToQuestionList(csvReader, Column.question("en"), Column.sparqlQuery(), Column.ignore(), Column.answerType(), Column.aggregationFlag(),
		        Column.onlyDboFlag(), Column.hybridFlag(), Column.keywords("en"));

		/**
		 * Dismiss specific question
		 */
		IQuestion removeMe = null;
		for (IQuestion q : que) {

			if (q.getLanguageToQuestion().get("en").equals("How old was Shockley when he died?")) {
				removeMe = q;
			}

		}
		que.remove(removeMe);

		/**
		 * set missing prefixes and check query
		 */
		for (IQuestion q : que) {
			String newSparql = SPARQLPrefixResolver.addMissingPrefixes(q.getSparqlQuery());
			q.setSparqlQuery(newSparql);
			if (!SPARQL.isValidSparqlQuery(newSparql)) {
				throw new Exception("Sparql not valid " + q.getLanguageToQuestion().get("en"));
			}
		}
		/**
		 * retrieve answers
		 */
		AnswerSyncer.syncAnswers(que, SPARQLEndpoints.DBPEDIA_ORG);

		int id = 1;
		for (IQuestion q : que) {
			/**
			 * check if all went well
			 */
			if (q.getGoldenAnswers().isEmpty()) {
				throw new Exception("Answerset empty");
			}
			/**
			 * set new IDs
			 */
			q.setId("" + id++);
		}
		/**
		 * write json
		 */
		QaldJson json = EJQuestionFactory.getQaldJson(que);
		EJDataset header = new EJDataset();
		header.setId("qald-8-test-multilingual");
		json.setDataset(header);

		ExtendedQALDJSONLoader.writeJson(json, new File(parentDir, "qald-8-test-multilingual.json"), true);
		/**
		 * Write debug file
		 */
		File f2 = new File(parentDir, "qald8testGOOD_QUESTIONS.csv");
		f2.delete();
		FileWriter fw2 = new FileWriter(f2);

		CSVWriter wr2 = new CSVWriter(fw2, ';', '"');

		IQuestionCsvParser.questionListToCsv(wr2, true, que, Column.ID(), Column.question("en"), Column.sparqlQuery(), Column.goldenAnswers());

		wr2.close();
		fw2.close();
		System.out.println("qald8test done");

	}

}
