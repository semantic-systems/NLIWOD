package org.aksw.qa.commons.qald;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.sparql.SPARQL;
import org.aksw.qa.commons.sparql.ThreadedSPARQL;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class Qald7CreationTool {
	private final static String DBO_URI = "http://dbpedia.org/ontology/";
	private final static String RES_URI = "http://dbpedia.org/resource/";
	private Logger log = LoggerFactory.getLogger(this.getClass());
	/**
	 * QALD2_dbpedia is missing due to missing answers in file.
	 */

	private Set<Dataset> testSets = ImmutableSet.of(Dataset.QALD1_Test_dbpedia, Dataset.QALD3_Test_dbpedia, Dataset.QALD4_Test_Multilingual, Dataset.QALD5_Test_Multilingual,
	        Dataset.QALD6_Test_Multilingual, Dataset.QALD1_Train_dbpedia, Dataset.QALD2_Train_dbpedia, Dataset.QALD3_Train_dbpedia, Dataset.QALD4_Train_Multilingual, Dataset.QALD4_Train_Multilingual,
	        Dataset.QALD5_Train_Multilingual, Dataset.QALD6_Train_Multilingual);
	private ThreadedSPARQL sparql = new ThreadedSPARQL(90, SPARQL.ENDPOINT_DBPEIDA_ORG);
	int badQuestionCounter = 0;

	public List<IQuestion> getMultilingualTest() {
		List<IQuestion> allQuestions = new ArrayList<>();
		for (Dataset it : testSets) {
			allQuestions.addAll(LoaderController.load(it));
			System.out.println(it.toString());

		}
		return allQuestions;
	}

	public List<IQuestion> getMultilingualTrain() {
		List<Dataset> allTestDatasets = new ArrayList<>(Arrays.asList(Dataset.values()));

		for (Dataset it : allTestDatasets) {
			if (it.toString().contains("Train") && !(it.toString().contains("Hybrid"))) {
				System.out.println(it.toString());
			}
		}
		return null;
	}

	public List<IQuestion> getHybridTrain() {
		List<Dataset> allTestDatasets = new ArrayList<>(Arrays.asList(Dataset.values()));

		for (Dataset it : allTestDatasets) {
			if (it.toString().contains("Train") && (it.toString().contains("Hybrid"))) {
				System.out.println(it.toString());
			}
		}
		return null;
	}

	public List<IQuestion> getHybridTest() {
		List<Dataset> allTestDatasets = new ArrayList<>(Arrays.asList(Dataset.values()));

		for (Dataset it : allTestDatasets) {
			if (it.toString().contains("Test") && (it.toString().contains("Hybrid"))) {
				System.out.println(it.toString());
			}
		}
		return null;
	}

	public boolean isMultilingualConsistent(final IQuestion q) {
		boolean ret = true;
		// at least 6 languages
		ret &= checkAtleastSixLanguages(q);
		// at least 1 keyword in every language
		ret &= checkKeywordsPresent(q);
		// sparql query is set
		ret &= checkSparqlPresent(q);
		return ret;

	}

	private boolean checkSparqlPresent(final IQuestion q) {
		if (StringUtils.isEmpty(q.getSparqlQuery())) {
			return false;
		}
		return true;
	}

	private boolean checkKeywordsPresent(final IQuestion q) {
		for (List<String> it : q.getLanguageToKeywords().values()) {
			if (it.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private boolean checkAtleastSixLanguages(final IQuestion q) {
		if (q.getLanguageToQuestion().values().size() < 6) {
			return false;
		}
		return true;
	}

	// public boolean checkAnswerCorrectness(final IQuestion q) {
	// Set<String> answersFromFile = q.getGoldenAnswers();
	// Set<String> answersFromServerAsString = getAnswersFromServer(q);
	// if (!(answersFromFile.containsAll(answersFromServerAsString) &&
	// answersFromServerAsString.containsAll(answersFromFile))) {
	//
	// // File file = new File("C:/output/QuestionsWithWrongAnswers.txt");
	// //
	// // try {
	// // writer = new FileWriter(file, true);
	// // writer.append("\r\n################ \nAnswers from File\r\n" +
	// // answersFromFile.toString() + "\r\n## Answers from server\r\n" +
	// // answersFromServerAsString.toString());
	// // writer.flush();
	// // writer.close();
	// // didWrite = true;
	// // } catch (IOException e) {
	// // e.printStackTrace();
	// // }
	//
	// return false;
	// }
	//
	// return false;
	// }

	public Set<String> getAnswersFromServer(final IQuestion q) throws ExecutionException {
		Set<RDFNode> answersFromServer = sparql.sparql(q.getSparqlQuery());
		answersFromServer = answersFromServer == null ? Collections.emptySet() : answersFromServer;
		Set<String> answersFromServerAsString = new HashSet<>();
		for (RDFNode it : answersFromServer) {
			if (it.isResource()) {
				answersFromServerAsString.add(it.asResource().getURI());
			} else {
				answersFromServerAsString.add(it.asLiteral().getString());
			}

		}
		return answersFromServerAsString;
	}

	public void addSave(final Map<IQuestion, Set<Fail>> map, final IQuestion q, final Fail fail) {
		if (map.get(q) == null) {
			map.put(q, new HashSet<Fail>());
		}
		map.get(q).add(fail);
	}

	public void createFileReport(final Set<Dataset> datasets, final String path, final boolean appendAnswerSets, final boolean skipQuestionsWithTooFewLanguages) {
		String n = System.getProperty("line.separator");
		Map<IQuestion, Set<Fail>> questionToFail = new HashMap<>();
		StringBuilder out = new StringBuilder();
		int questionCount = 0;
		int questionIterator = 1;
		for (Dataset dataset : datasets) {
			List<IQuestion> questions = LoaderController.load(dataset);
			questionCount += questions.size();
			for (IQuestion question : questions) {
				log.debug("Processing Question " + questionIterator++ + "/" + questionCount);

				((Question) question).setFromDataset(dataset);
				/**
				 * Checking flags
				 */

				if (!checkAtleastSixLanguages(question)) {
					if (skipQuestionsWithTooFewLanguages) {
						continue;
					}
					addSave(questionToFail, question, Fail.MISSING_LANGUAGES);
				}
				if (!checkKeywordsPresent(question)) {
					addSave(questionToFail, question, Fail.MISSING_KEYWORDS);
				}
				if (!checkSparqlPresent(question)) {
					addSave(questionToFail, question, Fail.SPARQL_MISSING);
				} else {
					try {
						if (!(checkIsOnlydbo(question.getSparqlQuery()) == question.getOnlydbo())) {
							addSave(questionToFail, question, Fail.ISONLYDBO_WRONG);
						}
					} catch (QueryParseException e) {
						addSave(questionToFail, question, Fail.SPARQL_PRASE_ERROR);
					}
				}

				/**
				 * checking answers
				 */
				Set<String> answersFromServer = Collections.emptySet();
				try {
					answersFromServer = getAnswersFromServer(question);
				} catch (ExecutionException e) {
					addSave(questionToFail, question, Fail.SPARQL_NOT_EXECUTABLE);
				}

				Set<String> answersFromDataset = question.getGoldenAnswers();
				answersFromDataset = answersFromDataset == null ? Collections.emptySet() : answersFromDataset;

				if (answersFromDataset.isEmpty()) {
					addSave(questionToFail, question, Fail.NO_ANSWERS_IN_DATASET);
				}
				if (!(answersFromDataset.containsAll(answersFromServer) && answersFromServer.containsAll(answersFromDataset))) {
					addSave(questionToFail, question, Fail.ANSWERSET_DIFFERS);

				}
				Set<String> inDatasetNotinServer = new HashSet<>(answersFromDataset);
				Set<String> inServerNotinDataset = new HashSet<>(answersFromServer);
				inDatasetNotinServer.removeAll(answersFromServer);
				inServerNotinDataset.removeAll(answersFromDataset);

				/**
				 * Creating output
				 */

				if ((questionToFail.get(question) != null) && !questionToFail.isEmpty()) {
					out.append("_____________________________________________________" + n);
					out.append("|  Question Dataset: " + ((Question) question).getFromDataset().name() + " Id: " + question.getId() + n);
					out.append(line("|  Flags: " + questionToFail.get(question).toString() + n));
					if (appendAnswerSets && (!inDatasetNotinServer.isEmpty() || !inServerNotinDataset.isEmpty())) {
						out.append("| Question: " + question.getLanguageToQuestion().get("en") + n);
						out.append("| Sparql Query:" + n);
						out.append("| " + question.getSparqlQuery().replaceAll("\\s", " ") + n);
						out.append("| Answers in dataset and not in Server response" + n);
						out.append("|" + inDatasetNotinServer.toString() + n + n);
						out.append("| Answers in Server response and not in Dataset" + n);
						out.append("|" + inServerNotinDataset.toString() + n);
					}
					out.append("_____________________________________________________" + n);

				}

			} // end for questions
		} // end for datasets

		out.append("From Datasets :" + datasets.toString() + n);
		out.append("Questions total in all Datasets: " + questionCount + " Faulty: " + questionToFail.size());

		File file = new File(path);
		FileWriter writer;
		try {
			writer = new FileWriter(file);
			writer.write(out.toString());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String line(final String in) {
		String n = System.getProperty("line.separator");
		StringBuilder out = new StringBuilder();
		for (int i = 1; i <= in.length(); i++) {
			if ((i % 100) == 0) {
				out.append(n + "|  ");
			}
			out.append(in.charAt(i - 1));
		}
		return out.toString();
	}

	private enum Fail {
		ISONLYDBO_WRONG,
		MISSING_KEYWORDS,
		MISSING_LANGUAGES,
		ANSWERSET_DIFFERS,
		SPARQL_PRASE_ERROR,
		SPARQL_MISSING,
		SPARQL_NOT_EXECUTABLE,
		NO_ANSWERS_IN_DATASET

	}

	private boolean checkIsOnlydbo(final String sparqlQuery) throws QueryParseException {
		if (sparqlQuery == null) {
			return false;
		}
		Query q = QueryFactory.create(sparqlQuery);
		PrefixMapping prefixMap = q.getPrefixMapping();
		Map<String, String> map = new HashMap<>(prefixMap.getNsPrefixMap());

		Set<Entry<String, String>> remove = new HashSet<>();
		for (Entry<String, String> it : map.entrySet()) {
			if (it.getKey().equals("rdf") || it.getKey().equals("rdfs") || it.getValue().equals(DBO_URI) || it.getValue().equals(RES_URI)) {
				remove.add(it);
			}
		}
		map.entrySet().removeAll(remove);
		return map.isEmpty();
	}

	public static void main(final String[] args) {

		// System.out.println("MultilingualTrain");
		// new Qald7CreationTool().getMultilingualTrain();
		// System.out.println("MultiolingualTest");
		// new Qald7CreationTool().getMultilingualTest();
		// System.out.println("HybridTrain");
		// new Qald7CreationTool().getHybridTrain();
		// System.out.println("HybridTest");
		// new Qald7CreationTool().getHybridTest();
		// System.out.println("Checking");

		Qald7CreationTool tool = new Qald7CreationTool();
		// List<IQuestion> qs =
		// LoaderController.load(Dataset.QALD3_Test_dbpedia);
		// Collections.shuffle(qs);
		// File file = new File("C:/output/QuestionsWithWrongAnswers.txt");
		// if (file.exists()) {
		// file.delete();
		// }
		// int i = 1;
		// for (IQuestion q : qs) {
		//
		// System.out.println("Question " + i++ + "/" + qs.size() + " (" +
		// tool.badQuestionCounter + " bad)");
		// }
		// tool.sparql.destroy();
		// System.out.println("Wrote file? " + tool.didWrite);
		// System.out.println("BadQuestions:" + tool.badQuestionCounter);
		// tool.createFileReport(new
		// HashSet<>(Arrays.asList(Dataset.QALD3_Test_dbpedia)),
		// "C:/output/QuestionsWithWrongAnswers.txt");
		boolean appendAnswerSets = true;
		boolean skipQuestionsWithInsufficientLanguages = true;
		tool.createFileReport(tool.testSets, "C:/output/QuestionsWithWrongAnswers.txt", appendAnswerSets, skipQuestionsWithInsufficientLanguages);
		tool.sparql.destroy();
		System.out.println("Done");
	}

}
