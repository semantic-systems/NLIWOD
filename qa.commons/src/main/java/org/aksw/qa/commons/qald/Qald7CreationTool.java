package org.aksw.qa.commons.qald;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.load.json.EJDataset;
import org.aksw.qa.commons.load.json.EJQuestionFactory;
import org.aksw.qa.commons.load.json.ExtendedJson;
import org.aksw.qa.commons.load.json.ExtendedQALDJSONLoader;
import org.aksw.qa.commons.load.json.QaldJson;
import org.aksw.qa.commons.sparql.SPARQLEndpoints;
import org.aksw.qa.commons.sparql.ThreadedSPARQL;
import org.aksw.qa.commons.store.StoreQALDXML;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.shared.PrefixMapping;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;

public class Qald7CreationTool {
	private final static String DBO_URI = "http://dbpedia.org/ontology/";
	private final static String RES_URI = "http://dbpedia.org/resource/";
	private int duplicate = 0;
	/**
	 * QALD1 and QALD2 not multilingual!
	 */
	public static final Set<Dataset> MULTILINGUAL_TRAIN_TEST_SETS = ImmutableSet.of(Dataset.QALD3_Test_dbpedia, Dataset.QALD4_Test_Multilingual, Dataset.QALD5_Test_Multilingual,
	        Dataset.QALD6_Test_Multilingual, Dataset.QALD3_Train_dbpedia, Dataset.QALD4_Train_Multilingual, Dataset.QALD4_Train_Multilingual, Dataset.QALD5_Train_Multilingual,
	        Dataset.QALD6_Train_Multilingual);
	public static final Set<Dataset> HYBRID_SETS = ImmutableSet.of(Dataset.QALD4_Test_Hybrid, Dataset.QALD4_Train_Hybrid, Dataset.QALD5_Test_Hybrid, Dataset.QALD5_Train_Hybrid,
	        Dataset.QALD6_Test_Hybrid, Dataset.QALD6_Train_Hybrid);

	private final ThreadedSPARQL sparql;

	public Qald7CreationTool() {
		sparql = new ThreadedSPARQL(90, SPARQLEndpoints.DBPEDIA_ORG);
	}

	public Qald7CreationTool(final String sparqlEndpoint, final int timeout) {
		sparql = new ThreadedSPARQL(timeout, sparqlEndpoint);
	}

	int badQuestionCounter = 0;

	/**
	 * Returns all Hybrid questions for Qald7 (Loads all previous qald hybrid questions and drops duplicates). <b> This will set "hybrid:true" in all questions!!!</b>
	 *
	 * @param datasets
	 *            All datasets from which questions should be extracted
	 * @return All available unique questions from given datasets
	 */
	public Set<Qald7Question> getQald7HybridQuestions(final Set<Dataset> datasets) {
		Set<Qald7Question> out = new HashSet<>();
		/**
		 * Qald7Question has english question hashcode as hash. so simply adding them to a set filters duplicates.
		 */
		for (Dataset dataset : datasets) {
			for (Qald7Question it : Qald7QuestionFactory.createInstances(LoaderController.load(dataset))) {
				it.setHybrid(true);
				out.add(it);
			}

		}
		return out;

	}

	/**
	 * Creates the hybrid datasets. Three files will be stored in given location: QALD-Json, Extended-Json and xml
	 *
	 * @param hybridDatasets
	 *            The sets questions are taken from.
	 * @param path
	 *            The path to write the datasets to.
	 * @param filenameWithoutExtension
	 *            The name of the new dataset
	 */
	public void createQald7HybridDataset(final Set<Dataset> hybridDatasets, final String path, final String filenameWithoutExtension) {
		this.createQald7Dataset(getQald7HybridQuestions(hybridDatasets), path, filenameWithoutExtension);
	}

	/**
	 * Creates the multilingual train datasets. Three files will be stored in given location: QALD-Json, Extended-Json and xml
	 *
	 * @param datasets
	 *            The sets questions are taken from.
	 * @param autocorrectOnlydbo
	 *            Is a bad Onlydbo-flag a exclusion criterion (Question wont appear in file) for a question or should it be autofixed?
	 * @param path
	 *            The path to write the datasets to.
	 * @param filenameWithoutExtension
	 *            The name of the new dataset
	 */
	public void createQald7MultilingualTrainDataset(final Set<Dataset> datasets, final boolean fileReport, final boolean autocorrectOnlydbo, final String path, final String filenameWithoutExtension) {
		Set<Qald7Question> questions = loadAndAnnotateTrain(datasets, autocorrectOnlydbo);

		this.createQald7Dataset(extractGoodTrainQuestionsFromAnnotated(questions), path, filenameWithoutExtension);
		if (fileReport) {
			this.createFileReport(questions, path + "BadQuestionsfileReport.txt", new HashSet<Fail>());
		}

	}

	/**
	 * Loads all questions from given datasets, checks question integrity (is the stored answerset still identical with the one returned for given sparql query, is a sparql present and parseable, are
	 * at least 6 languages available with keywords, is an answertype set,... ) Also, duplicates are filtered out, only the candidate with the least error flags @link {@link Fail} will be in returned
	 * set. So, returned Questions are all clean. To get a duplicate free, with {@link Fail} annotated dataset, use {@link #loadAndAnnotateTrain(Set, boolean)}
	 *
	 * @param datasets
	 *            The datasets from which the questions are gathered
	 * @param autocorrectOnlydbo
	 *            Is a bad Onlydbo-flag a exclusion criterion (Question wont appear in file) for a question or should it be autofixed?
	 * @return All clean duplicate free questions from given datasets.
	 */
	public Set<Qald7Question> getQald7MultilingualTrainQuestions(final Set<Dataset> datasets, final boolean autocorrectOnlydbo) {
		return extractGoodTrainQuestionsFromAnnotated(loadAndAnnotateTrain(datasets, autocorrectOnlydbo));
	}

	public Set<Qald7Question> loadAndAnnotateTrain(final Set<Dataset> datasets, final boolean autocorrectOnlyDBO) {
		List<Qald7Question> allDatasetQuestions = new ArrayList<>();
		for (Dataset dataset : datasets) {
			List<Qald7Question> questions = Qald7QuestionFactory.createInstances(LoaderController.load(dataset));
			for (Qald7Question question : questions) {
				allDatasetQuestions.add(question);
				question.setFromDataset(dataset);
				question.setFails(new HashSet<Fail>());

				if (!checkAtleastSixLanguages(question)) {

					question.addFail(Fail.MISSING_LANGUAGES);
				}
				if (!checkKeywordsPresent(question)) {
					question.addFail(Fail.MISSING_KEYWORDS);
				}
				if (!checkAnswertypeSet(question)) {
					question.addFail(Fail.ANSWERTYPE_NOT_SET);
				}

				if (!checkSparqlPresent(question)) {
					question.addFail(Fail.SPARQL_MISSING);
				} else {
					try {
						if (!(checkIsOnlydbo(question.getSparqlQuery()) == question.getOnlydbo())) {
							if (autocorrectOnlyDBO) {
								question.setOnlydbo(checkIsOnlydbo(question.getSparqlQuery()));
							} else {
								question.addFail(Fail.ISONLYDBO_WRONG);
							}

						}
					} catch (QueryParseException e) {
						question.addFail(Fail.SPARQL_PARSE_ERROR);
					}
				} // end if sparql missing

				/**
				 * checking answers
				 */
				Set<String> answersFromServer = Collections.emptySet();
				try {
					answersFromServer = getAnswersFromServer(question);

				} catch (ExecutionException e) {
					question.addFail(Fail.SPARQL_NOT_EXECUTABLE);
				}
				question.setServerAnswers(answersFromServer);
				Set<String> answersFromDataset = question.getGoldenAnswers();
				answersFromDataset = answersFromDataset == null ? Collections.emptySet() : answersFromDataset;

				if (answersFromDataset.isEmpty()) {
					question.addFail(Fail.NO_ANSWERS_IN_DATASET);
				}
				if (!(answersFromDataset.containsAll(answersFromServer) && answersFromServer.containsAll(answersFromDataset))) {
					question.addFail(Fail.ANSWERSET_DIFFERS);

				}

			} // end question for

		} // end dataset for

		return this.findAndSelectBestDuplicate(allDatasetQuestions);

	}

	private boolean checkSparqlPresent(final IQuestion q) {
		return !StringUtils.isEmpty(q.getSparqlQuery());
	}

	private boolean checkAnswertypeSet(final IQuestion q) {
		return !Strings.isNullOrEmpty(q.getAnswerType());
	}

	private boolean checkKeywordsPresent(final IQuestion q) {
		for (String it : q.getLanguageToQuestion().keySet()) {
			if ((q.getLanguageToKeywords().get(it) == null) || q.getLanguageToKeywords().get(it).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private boolean checkAtleastSixLanguages(final IQuestion q) {
		return !(q.getLanguageToQuestion().values().size() < 6);
	}

	/**
	 * Returns answers from official dbpedia endpoint to the stored sparql in {@link IQuestion}
	 *
	 * @param q
	 *            Question to be answered
	 * @return Answers as string set
	 * @throws ExecutionException
	 */
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

	private void addSave(final Map<String, List<Qald7Question>> map, final String question, final Qald7Question q) {
		if (map.get(question) == null) {
			map.put(question, new ArrayList<>());
		}
		map.get(question).add(q);
	}

	private Set<Qald7Question> findAndSelectBestDuplicate(final List<Qald7Question> questions) {
		this.duplicate = 0;
		Set<Qald7Question> out = new HashSet<>();
		Map<String, List<Qald7Question>> questionToObject = new HashMap<>();

		/**
		 * Adding all questions to "multimap"
		 */
		for (Qald7Question it : questions) {
			addSave(questionToObject, it.getLanguageToQuestion().get("en"), it);
		}
		/**
		 * If no english question is stored
		 */
		try {
			out.addAll(questionToObject.get(null));
			questionToObject.remove(null);
		} catch (NullPointerException e) {

		}

		for (String it : questionToObject.keySet()) {
			if (questionToObject.get(it).size() == 1) {
				/**
				 * Add all uniques
				 */
				out.addAll(questionToObject.get(it));
			} else {
				/**
				 * Duplicate handling starts here
				 */
				int min = Integer.MAX_VALUE;
				duplicate += questionToObject.get(it).size() - 1;
				for (Qald7Question q : questionToObject.get(it)) {
					if (q.getFails().size() < min) {
						min = q.getFails().size();
					}
				}
				for (Qald7Question q : questionToObject.get(it)) {

					if (q.getFails().size() == min) {
						out.add(q);

						break;
					}
				}

			}
		}

		return out;
	}

	private Set<Qald7Question> extractGoodTrainQuestionsFromAnnotated(final Set<Qald7Question> questions) {
		Set<Qald7Question> goodQuestions = new HashSet<>();
		for (Qald7Question question : questions) {
			if (question.getFails().isEmpty()) {
				goodQuestions.add(question);
			}
		}
		return goodQuestions;
	}

	private Set<Qald7Question> extractBadQuestionsFromAnnotated(final Set<Qald7Question> questions, final Set<Fail> ignoreFlags) {
		Set<Qald7Question> badQuestions = new HashSet<>();
		for (Qald7Question question : questions) {
			Set<Fail> flags = new HashSet<>(question.getFails());
			flags.removeAll(ignoreFlags);

			if (!flags.isEmpty()) {
				badQuestions.add(question);
			}
		}
		return badQuestions;
	}

	private void createQald7Dataset(final Set<Qald7Question> allQuestions, final String path, final String filenameWithoutExtension) {
		List<IQuestion> goodQuestions = new ArrayList<>();
		int newId = 0;
		for (Qald7Question question : allQuestions) {
			question.setId("" + newId++);
			goodQuestions.add(question);
		}

		EJDataset jsonDataset = new EJDataset();
		jsonDataset.setId(filenameWithoutExtension);

		QaldJson qaldJson = EJQuestionFactory.getQaldJson(goodQuestions);
		qaldJson.setDataset(jsonDataset);
		ExtendedJson extendedJson = EJQuestionFactory.getExtendedJson(goodQuestions);
		extendedJson.setDataset(jsonDataset);
		try {
			ExtendedQALDJSONLoader.writeJson(qaldJson, new File(path + "QaldJson_" + filenameWithoutExtension + ".json"), true);
			ExtendedQALDJSONLoader.writeJson(extendedJson, new File(path + "ExtendedJson_" + filenameWithoutExtension + ".json"), true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			StoreQALDXML xml = new StoreQALDXML(filenameWithoutExtension);
			for (IQuestion q : goodQuestions) {
				xml.write(q);
			}
			xml.close(path + "XML_" + filenameWithoutExtension + ".xml", filenameWithoutExtension);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Creates a file report to all bad questions in given datasets
	 *
	 * @param datasets
	 *            All datasets to be checked
	 * @param autocorrectOnlydbo
	 *            Is a bad Onlydbo-flag a exclusion criterion (Question wont appear in file) for a question or should it be autofixed?
	 * @param pathAndFilenameWithExtension
	 *            Path and name of new file report
	 * @param skipQuestionsWithTooLittleLanguages
	 *            Normally, multilingual datasets have at least six languages. When this flag is set, all questions with less languages will be ignored, otherwise its an error {@link Fail} and the
	 *            question goes into the report
	 */
	public void createFileReportForTestQuestions(final Set<Dataset> datasets, final boolean autocorrectOnlydbo, final String pathAndFilenameWithExtension, final Set<Fail> ignoreFlags) {
		createFileReport(loadAndAnnotateTrain(datasets, autocorrectOnlydbo), pathAndFilenameWithExtension, ignoreFlags);

	}

	public void createFileReport(final Set<Qald7Question> allQuestions, final String pathAndFilenameWithExtension, final Set<Fail> ignoreFlags) {
		String n = System.getProperty("line.separator");
		Set<Qald7Question> badQuestions = extractBadQuestionsFromAnnotated(allQuestions, ignoreFlags);
		StringBuilder out = new StringBuilder();
		Set<Dataset> datasets = new HashSet<>();
		for (Qald7Question question : badQuestions) {
			datasets.add(question.getFromDataset());
			Set<String> answersFromServer = question.getServerAnswers();
			Set<String> answersFromDataset = question.getGoldenAnswers();
			answersFromDataset = answersFromDataset == null ? Collections.emptySet() : answersFromDataset;
			answersFromServer = answersFromServer == null ? Collections.emptySet() : answersFromServer;
			Set<String> inDatasetNotinServer = new HashSet<>(answersFromDataset);
			Set<String> inServerNotinDataset = new HashSet<>(answersFromServer);
			inDatasetNotinServer.removeAll(answersFromServer);
			inServerNotinDataset.removeAll(answersFromDataset);

			/**
			 * Creating output
			 */
			Set<Fail> flags = new HashSet<>(question.getFails());
			flags.removeAll(ignoreFlags);
			out.append("_____________________________________________________" + n);
			out.append("| Question Dataset: " + question.getFromDataset().name() + " Id: " + question.getId() + n);
			out.append("| Flags: " + flags.toString() + n);
			out.append("| Question: " + question.getLanguageToQuestion().get("en") + n);
			if ((!inDatasetNotinServer.isEmpty() || !inServerNotinDataset.isEmpty())) {
				out.append("| Sparql Query:" + n);
				out.append("| " + question.getSparqlQuery().replaceAll("\\s", " ") + n);
				out.append("| Answers in dataset and not in Server response" + n);
				out.append("|" + inDatasetNotinServer.toString() + n + n);
				out.append("| Answers in Server response and not in Dataset" + n);
				out.append("|" + inServerNotinDataset.toString() + n);
			}
			out.append("_____________________________________________________" + n);

		} // end for badQuestions

		out.append("From Datasets :" + datasets.toString() + n);
		out.append("Unique Questions total in all Datasets: " + allQuestions.size() + " Faulty: " + badQuestions.size() + " beforehand removed duplicates: " + duplicate);

		File file = new File(pathAndFilenameWithExtension);
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

	/**
	 * Call this if you dont need this object anymore. Closes the Threads around the server connection to the sparql server.
	 */
	public void destroy() {
		this.sparql.destroy();
	}

	public static void main(final String[] args) {
		Qald7CreationTool tool = new Qald7CreationTool();
		tool.createQald7HybridDataset(HYBRID_SETS, "", "qald-7-train-hybrid");
		tool.createQald7MultilingualTrainDataset(MULTILINGUAL_TRAIN_TEST_SETS, true, true, "", "qald-7-train-multilingual");
		tool.sparql.destroy();
		System.out.println("duplcates " + tool.duplicate);
		System.out.println("Done");
	}

}
