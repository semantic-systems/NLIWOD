package org.aksw.qa.commons.load;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * @author ricardousbeck tortugaattack
 *
 */
public class QALD_Loader {
	static Logger log = LoggerFactory.getLogger(QALD_Loader.class);

	private static InputStream getInputStream(final Dataset set) {
		// Magical get the path from qa-datasets

		try {
			URL url = mapDatasetToPath(set);
			return url.openStream();
		} catch (IOException e) {
			log.error("Couldnt open stream to dataset", e);
		} catch (NullPointerException e) {
			log.error("No Mapping for this Dataset " + set.toString(), e);
		}
		return null;
	}

	private static URL mapDatasetToPath(final Dataset set) {
		Class<?> loadingAnchor = null;
		try {
			loadingAnchor = Class.forName("org.aksw.qa.datasets.ResourceLoadingAnchor");
		} catch (ClassNotFoundException e) {
			log.error("Couldn't find the org.aksw.qa.datasets.ResourceLoadingAnchor class necessary to load the datases. Returning null.", e);
			return null;
		}

		switch (set) {
		case nlq:
			return loadingAnchor.getResource("/NLQ-OKBQA/nlq1_vis.json");

		case QALD1_Test_dbpedia:
			return loadingAnchor.getResource("/QALD-master/1/data/dbpedia-test.xml");
		case QALD1_Test_musicbrainz:
			return loadingAnchor.getResource("/QALD-master/1/data/musicbrainz-test.xml");
		case QALD1_Train_dbpedia:
			return loadingAnchor.getResource("/QALD-master/1/data/dbpedia-train.xml");
		case QALD1_Train_musicbrainz:
			return loadingAnchor.getResource("/QALD-master/1/data/musicbrainz-train.xml");

		case QALD2_Test_dbpedia:
			return loadingAnchor.getResource("/QALD-master/2/data/dbpedia-test.xml");
		case QALD2_Test_musicbrainz:
			return loadingAnchor.getResource("/QALD-master/2/data/musicbrainz-test.xml");
		case QALD2_Train_dbpedia:
			return loadingAnchor.getResource("/QALD-master/2/data/dbpedia-train.xml");
		case QALD2_Train_musicbrainz:
			return loadingAnchor.getResource("/QALD-master/2/data/musicbrainz-train.xml");

		case QALD3_Test_dbpedia:
			return loadingAnchor.getResource("/QALD-master/3/data/dbpedia-test.xml");
		case QALD3_Test_esdbpedia:
			return loadingAnchor.getResource("/QALD-master/3/data/esdbpedia-test.xml");
		case QALD3_Test_musicbrainz:
			return loadingAnchor.getResource("/QALD-master/3/data/musicbrainz-test.xml");
		case QALD3_Train_dbpedia:
			return loadingAnchor.getResource("/QALD-master/3/data/dbpedia-train.xml");
		case QALD3_Train_esdbpedia:
			return loadingAnchor.getResource("/QALD-master/3/data/esdbpedia-train.xml");
		case QALD3_Train_musicbrainz:
			return loadingAnchor.getResource("/QALD-master/3/data/musicbrainz-train.xml");

		case QALD4_Test_Hybrid:
			return loadingAnchor.getResource("/QALD-master/4/data/qald-4_hybrid_test_withanswers.xml");
		case QALD4_Test_Multilingual:
			return loadingAnchor.getResource("/QALD-master/4/data/qald-4_multilingual_test_withanswers.xml");
		case QALD4_Test_biomedical:
			return loadingAnchor.getResource("/QALD-master/4/data/qald-4_biomedical_test.xml");
		case QALD4_Train_Hybrid:
			return loadingAnchor.getResource("/QALD-master/4/data/qald-4_hybrid_train.xml");
		case QALD4_Train_Multilingual:
			return loadingAnchor.getResource("/QALD-master/4/data/qald-4_multilingual_train_withanswers.xml");
		case QALD4_Train_biomedical:
			return loadingAnchor.getResource("/QALD-master/4/data/qald-4_biomedical_train.xml");

		case QALD5_Test_Hybrid:
		case QALD5_Test_Multilingual:
			return loadingAnchor.getResource("/QALD-master/5/data/qald-5_test.xml");
		case QALD5_Train_Hybrid:
		case QALD5_Train_Multilingual:
			return loadingAnchor.getResource("/QALD-master/5/data/qald-5_train.xml");

		case QALD6_Train_Hybrid:
			return loadingAnchor.getResource("/QALD-master/6/data/qald-6-train-hybrid.json");
		case QALD6_Train_Multilingual:
			return loadingAnchor.getResource("/QALD-master/6/data/qald-6-train-multilingual.json");

		// case qbench1:
		// return
		// ClassLoader.getSystemClassLoader().getResource("qbench/qbench1.xml");
		// case qbench2:
		// return
		// ClassLoader.getSystemClassLoader().getResource("qbench/qbench2.xml");
		// case stonetemple:
		// return
		// ClassLoader.getSystemClassLoader().getResource("stonetemple/stonetemple");
		// FIXME datacube und qbench sollte gleich sein?!Konrad Höffner
		// Fragen
		// case QALD6_Train_Datacube:
		// return
		// ClassLoader.getSystemClassLoader().getResource("QALD-6/qald-6-train-datacube.json");

		default:
			break;

		}
		return null;
	}

	public static List<IQuestion> load(final Dataset data) {
		try {
			InputStream is = null;
			is = getInputStream(data);
			if (is == null) {
				log.error("Couldn't load dataset " + data.name() + ". Returning null.");
				return null;
			}
			List<IQuestion> out = null;
			if (is.available() > 0) // check if stream is not empty
			{

				List<IQuestion> hybrid;
				List<IQuestion> loadedQ;
				switch (data) {

				case QALD1_Test_dbpedia:
				case QALD1_Test_musicbrainz:
				case QALD1_Train_dbpedia:
				case QALD1_Train_musicbrainz:
				case QALD2_Test_dbpedia:
				case QALD2_Test_musicbrainz:
				case QALD2_Train_dbpedia:
				case QALD2_Train_musicbrainz:
				case QALD3_Test_dbpedia:
				case QALD3_Test_esdbpedia:
				case QALD3_Test_musicbrainz:
				case QALD3_Train_dbpedia:
				case QALD3_Train_esdbpedia:
				case QALD3_Train_musicbrainz:
				case QALD4_Test_Hybrid:
				case QALD4_Test_Multilingual:
				case QALD4_Test_biomedical:
				case QALD4_Train_Hybrid:
				case QALD4_Train_Multilingual:
				case QALD4_Train_biomedical:
					out = loadXML(is);
					break;

				case QALD5_Test_Hybrid:
				case QALD5_Train_Hybrid:
					hybrid = new ArrayList<>();
					loadedQ = loadXML(is);
					for (IQuestion q : loadedQ) {
						if (q.getHybrid()) {
							hybrid.add(q);
						}
					}
					out = hybrid;
					break;

				case QALD5_Test_Multilingual:
				case QALD5_Train_Multilingual:
					hybrid = new ArrayList<>();
					loadedQ = loadXML(is);
					for (IQuestion q : loadedQ) {
						if (!q.getHybrid()) {
							hybrid.add(q);
						}
					}
					out = hybrid;
					break;
				case QALD6_Train_Hybrid:
				case QALD6_Train_Multilingual:
					out = loadJSON(is);
					break;
				case nlq:
					out = loadNLQ(is);
					break;
				}
				is.close();
				return out;
			} else {
				throw new IOException("InputStream is null");
			}

		} catch (IOException e) {
			log.info("Couldnt load dataset ", e);
		}
		return null;
	}

	// TODO separate checking for non-empty input stream in all separate load
	// instances necessary? -Christian
	// oldtodo check that input stream is not empty before parsing(Deez Nuts!)
	/**
	 * This methods loads QALD XML files (used in QALD 1 to QALD 5)
	 * 
	 * @param file
	 * @return
	 */
	public static List<IQuestion> loadXML(final InputStream file) {
		List<IQuestion> questions = new ArrayList<>();

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc;
			doc = db.parse(file);
			doc.getDocumentElement().normalize();

			NodeList questionNodes = doc.getElementsByTagName("question");

			for (int i = 0; i < questionNodes.getLength(); i++) {

				IQuestion question = new Question();
				Element questionNode = (Element) questionNodes.item(i);

				question.setId(Integer.valueOf(questionNode.getAttribute("id")));
				question.setAnswerType(questionNode.getAttribute("answertype"));
				question.setAggregation(Boolean.valueOf(questionNode.getAttribute("aggregation")));
				question.setOnlydbo(Boolean.valueOf(questionNode.getAttribute("onlydbo")));
				question.setHybrid(Boolean.valueOf(questionNode.getAttribute("hybrid")));

				// Read question
				NodeList nlrs = questionNode.getElementsByTagName("string");
				for (int j = 0; j < nlrs.getLength(); j++) {
					String lang = ((Element) nlrs.item(j)).getAttribute("lang");
					question.getLanguageToQuestion().put(lang, ((Element) nlrs.item(j)).getTextContent().trim());
				}

				// read keywords
				NodeList keywords = questionNode.getElementsByTagName("keywords");
				for (int j = 0; j < keywords.getLength(); j++) {
					String lang = ((Element) keywords.item(j)).getAttribute("lang");
					question.getLanguageToKeywords().put(lang, Arrays.asList(((Element) keywords.item(j)).getTextContent().trim().split(", ")));
				}

				// Read pseudoSPARQL query
				Element element = (Element) questionNode.getElementsByTagName("pseudoquery").item(0);
				if (element != null && element.hasChildNodes()) {
					NodeList childNodes = element.getChildNodes();
					Node item = childNodes.item(0);
					question.setPseudoSparqlQuery(item.getNodeValue().trim());
				}

				// Read SPARQL query
				// checks also that the text node containing query is not
				// null
				element = (Element) questionNode.getElementsByTagName("query").item(0);
				if (element != null && element.hasChildNodes()) {
					NodeList childNodes = element.getChildNodes();
					Node item = childNodes.item(0);
					question.setSparqlQuery(item.getNodeValue().trim());
				}
				// check if OUT OF SCOPE marked
				if (question.getPseudoSparqlQuery() != null) {
					question.setOutOfScope(question.getPseudoSparqlQuery().toUpperCase().contains("OUT OF SCOPE"));
				}
				// check if OUT OF SCOPE marked
				if (question.getSparqlQuery() != null) {
					question.setOutOfScope(question.getSparqlQuery().toUpperCase().contains("OUT OF SCOPE"));
				}
				// Read answers
				NodeList answers = questionNode.getElementsByTagName("answers");
				HashSet<String> set = new HashSet<>();
				for (int j = 0; j < answers.getLength(); j++) {
					NodeList answer = ((Element) answers.item(j)).getElementsByTagName("answer");
					for (int k = 0; k < answer.getLength(); k++) {
						String answerString = ((Element) answer.item(k)).getTextContent();
						set.add(answerString.trim());
					}
				}
				question.setGoldenAnswers(set);

				questions.add(question);
			}

		} catch (DOMException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return questions;
	}

	/**
	 * This method loads QALD JSON files as used in QALD 6
	 * 
	 * @param file
	 * @return
	 */
	public static List<IQuestion> loadJSON(final InputStream file) {
		// TODO Catch exceptions
		List<IQuestion> output = new ArrayList<>();
		try {
			JsonReader jsonReader = Json.createReader(file);
			JsonObject mainJsonObject = jsonReader.readObject();
			// JsonObject innerObject =jsonObject.getJsonObject("dataset");
			JsonArray jArray = mainJsonObject.getJsonArray("questions");

			String attributes[] = { "id", "aggregation", "answertype", "onlydbo", "hybrid" };

			for (JsonValue questionJsonObj : jArray) {
				JsonObject listObj = (JsonObject) questionJsonObj;
				IQuestion q = new Question();
				for (String attr : attributes) {
					if (listObj.containsKey(attr)) {
						String val = listObj.get(attr).toString().replace("\"", "");
						q.setValue(attr, val);
					}
				}
				output.add(q);

				JsonArray questionArray = listObj.getJsonArray("question");
				for (JsonValue questionVal : questionArray) {
					JsonObject questionObj = (JsonObject) questionVal;
					String lang = questionObj.getString("language");
					q.getLanguageToQuestion().put(lang, questionObj.getString("string").trim());
					if (questionObj.containsKey("keywords")) {
						List<String> keywords = Arrays.asList(questionObj.getString("keywords").split(","));
						q.getLanguageToKeywords().put(lang, keywords);
					}
				}

				JsonObject query = (JsonObject) listObj.get("query");
				if (query.containsKey("sparql")) {
					String strQuery = query.getString("sparql").trim();
					q.setSparqlQuery(strQuery);
				}
				if (query.containsKey("pseudo")) {
					String strQuery = query.getString("pseudo").trim();
					q.setPseudoSparqlQuery(strQuery);
				}

				JsonArray answerList = listObj.getJsonArray("answers");
				if (!answerList.isEmpty()) {
					JsonObject answerListHead = answerList.getJsonObject(0);
					JsonObject headObject = answerListHead.getJsonObject("head");
					JsonArray vars = headObject.getJsonArray("vars");

					Set<String> answers = new HashSet<>();
					if (!answerList.isEmpty()) {
						JsonObject answerObject = answerList.getJsonObject(0);
						if (answerObject.containsKey("boolean")) {
							answers.add(answerObject.get("boolean").toString());
						}
						if (answerObject.containsKey("results")) {
							JsonObject resultObject = answerObject.getJsonObject("results");
							JsonArray bindingsList = resultObject.getJsonArray("bindings");
							for (JsonValue bind : bindingsList) {
								JsonObject bindObj = (JsonObject) bind;
								for (JsonValue varName : vars) {
									String var = varName.toString().replaceAll("\"", "");
									if (bindObj.containsKey(var)) {
										JsonObject j = bindObj.getJsonObject(var);
										answers.add(j.getString("value").trim());
									}
								}

							} // end for bindingsList
						} // end if no result set
						q.setGoldenAnswers(answers);
					} // end if Answerlist emptiy

				} // end For questions
			}

		} catch (DOMException e) {
			e.printStackTrace();

		}

		/**
		 * Removing Questions with no answers
		 */
		boolean printHappend = false;
		String message = "";
		List<IQuestion> emptyQuestions = new ArrayList<>();
		for (IQuestion k : output) {
			if (k.getGoldenAnswers().isEmpty()) {
				emptyQuestions.add(k);
				if (!printHappend) {
					message += "Following Questions (id) have no attached answers: ";
					printHappend = true;
				}
				message += k.getId() + ", ";
			}
		}
		if (printHappend) {
			log.debug(message + " and will be removed");
		}
		output.removeAll(emptyQuestions);
		return output;
	}

	public static List<IQuestion> loadNLQ(final InputStream file) {

		List<IQuestion> output = new ArrayList<>();
		HashMap<Integer, ArrayList<JsonObject>> idToQuestion = new HashMap<>();
		try {
			if (file.available() > 0) // check if stream is not empty
			{
				JsonReader jsonReader = Json.createReader(file);
				JsonArray mainJsonArray = jsonReader.readArray();

				for (JsonValue currentJsonValue : mainJsonArray) {
					JsonObject currentObject = (JsonObject) currentJsonValue;
					try {
						Integer id = Integer.parseInt(currentObject.getString("id"));
						if (idToQuestion.containsKey(id)) {
							idToQuestion.get(id).add(currentObject);
						} else {
							ArrayList<JsonObject> jArray = new ArrayList<>();
							jArray.add(currentObject);
							idToQuestion.put(id, jArray);
						}
					} catch (NumberFormatException e) {
						log.info("Couldnt load question from dataset due to wrong or missing question-id", e);
					}
				}

			}
		} catch (IOException e) {
			log.error("Could not load Dataset", e);
		}

		for (Integer i : idToQuestion.keySet()) {
			Question q = new Question();
			for (JsonObject currentJsonObject : idToQuestion.get(i)) {
				q.setValue("id", currentJsonObject.getString("id"));
				// TODO this answer type needs to be mapped via switch case
				// q.setAnswerType(currentJsonObject.getString("type"));
				String lang = currentJsonObject.getString("lang");
				String questiion = currentJsonObject.getString("question");
				String answer = currentJsonObject.getString("answer");

				String sparql = currentJsonObject.getString("sparql");

				q.getLanguageToQuestion().put(lang, questiion);
				q.setSparqlQuery(lang, sparql);
				Set<String> answ = new HashSet<>();
				answ.add(answer);
				q.setGoldenAnswers(lang, answ);
			}
			output.add(q);
		}

		return output;
	}

	public static void main(final String[] args) {
		for (Dataset data : Dataset.values()) {
			List<IQuestion> questions = load(data);
			if (questions == null) {
				System.out.println("Dataset null" + data.toString());
			} else if (questions.size() == 0) {
				System.out.println("Dataset Empty" + data.toString());
			} else {
				System.out.println("LOADED SUCCESSFULLY " + data.toString());
			}
		}
	}
}
