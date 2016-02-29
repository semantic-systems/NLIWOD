//package org.aksw.autosparql.commons.qald;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;
//import javax.json.JsonReader;
//import javax.json.JsonValue;
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.aksw.hawk.datastructures.Question;
//import org.w3c.dom.DOMException;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//
///**
// *
// */
//public class QALD_Loader {
//
//    // public static void main(String[] args) {
//    //
//    // String file = new File("resources/qald-5_test.xml").getAbsolutePath();
//    // QALD_Loader ql = new QALD_Loader();
//    // List<Question> load = ql.load(file);
//    // int hybrid = 0;
//    // for (Question q : load) {
//    // // System.out.println(q.languageToQuestion);
//    // // System.out.println("\tAnswers: " +
//    // // StringUtils.join(q.goldenAnswers, ", "));
//    //
//    // if (q.hybrid) {
//    // if (q.answerType.equals("resource")) {
//    // if (q.onlydbo) {
//    // if (!q.aggregation) {
//    // System.out.println(q.id + "\t"
//    // + q.languageToQuestion.get("en"));
//    // }
//    // }
//    // }
//    // }
//    // }
//    // System.out.println(hybrid);
//    // }
//
//    public static void main(String args[]) {
//
//	URL url = ClassLoader.getSystemClassLoader().getResource(
//		"QALD-6/qald-6-train-multilingual.json");
//
//	List<Question> questions = null;
//	try {
//	    questions = QALD_Loader.loadJSON(url.openStream());
//
//	} catch (IOException e) {
//	    e.printStackTrace();
//	    System.exit(0);
//	}
//
//	for (Question q : questions) {
//	    System.out.println(q.toString());
//	}
//
//    }
//
//    public static List<Question> load(String file) {
//
//	List<Question> questions = new ArrayList<Question>();
//
//	try {
//
//	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//	    DocumentBuilder db = dbf.newDocumentBuilder();
//	    Document doc = db.parse(file);
//	    doc.getDocumentElement().normalize();
//
//	    NodeList questionNodes = doc.getElementsByTagName("question");
//
//	    for (int i = 0; i < questionNodes.getLength(); i++) {
//
//		Question question = new Question();
//		Element questionNode = (Element) questionNodes.item(i);
//
//		question.id = Integer.valueOf(questionNode.getAttribute("id"));
//		question.answerType = questionNode.getAttribute("answertype");
//		question.aggregation = Boolean.valueOf(questionNode
//			.getAttribute("aggregation"));
//		question.onlydbo = Boolean.valueOf(questionNode
//			.getAttribute("onlydbo"));
//		question.hybrid = Boolean.valueOf(questionNode
//			.getAttribute("hybrid"));
//
//		question.loadedAsASKQuery = new Boolean(false); // Update when
//								// query loaded
//
//		// Read question
//		NodeList nlrs = questionNode.getElementsByTagName("string");
//		for (int j = 0; j < nlrs.getLength(); j++) {
//		    String lang = ((Element) nlrs.item(j)).getAttribute("lang");
//		    question.languageToQuestion.put(lang,
//			    ((Element) nlrs.item(j)).getTextContent().trim());
//		}
//
//		// read keywords
//		NodeList keywords = questionNode
//			.getElementsByTagName("keywords");
//		for (int j = 0; j < keywords.getLength(); j++) {
//		    String lang = ((Element) keywords.item(j))
//			    .getAttribute("lang");
//		    question.languageToKeywords.put(
//			    lang,
//			    Arrays.asList(((Element) keywords.item(j))
//				    .getTextContent().trim().split(", ")));
//		}
//
//		// Read pseudoSPARQL query
//		Element element = (Element) questionNode.getElementsByTagName(
//			"pseudoquery").item(0);
//		if (element != null) {
//		    NodeList childNodes = element.getChildNodes();
//		    Node item = childNodes.item(0);
//		    question.pseudoSparqlQuery = item.getNodeValue().trim();
//
//		    question.loadedAsASKQuery = question.loadedAsASKQuery
//			    || new Boolean(
//				    QALD4_EvaluationUtils
//					    .isAskType(question.pseudoSparqlQuery));
//		}
//
//		// Read SPARQL query
//		element = (Element) questionNode.getElementsByTagName("query")
//			.item(0);
//		if (element != null) {
//		    NodeList childNodes = element.getChildNodes();
//		    Node item = childNodes.item(0);
//		    question.sparqlQuery = item.getNodeValue().trim();
//
//		    question.loadedAsASKQuery = question.loadedAsASKQuery
//			    || new Boolean(
//				    QALD4_EvaluationUtils
//					    .isAskType(question.sparqlQuery));
//		}
//		// check if OUT OF SCOPE marked
//		if (question.pseudoSparqlQuery != null) {
//		    question.outOfScope = question.pseudoSparqlQuery
//			    .toUpperCase().contains("OUT OF SCOPE");
//		}
//		// read answers
//		NodeList answers = questionNode.getElementsByTagName("answer");
//		HashSet<String> set = new HashSet<>();
//		for (int j = 0; j < answers.getLength(); j++) {
//		    String answer = ((Element) answers.item(j))
//			    .getTextContent();
//		    set.add(answer.trim());
//		}
//		question.goldenAnswers.put("en", set);
//		questions.add(question);
//	    }
//
//	} catch (DOMException e) {
//	    e.printStackTrace();
//	} catch (ParserConfigurationException e) {
//	    e.printStackTrace();
//	} catch (SAXException e) {
//	    e.printStackTrace();
//	} catch (IOException e) {
//	    e.printStackTrace();
//	}
//	return questions;
//    }
//
//    public static List<Question> loadJSON(InputStream file) {
//
//	List<Question> output = new ArrayList<Question>();
//
//	JsonReader jsonReader = Json.createReader(file);
//	JsonObject mainJsonObject = jsonReader.readObject();
//
//	// JsonObject innerObject =jsonObject.getJsonObject("dataset");
//
//	JsonArray jArray = mainJsonObject.getJsonArray("questions");
//
//	String attributes[] = { "id", "aggregation", "answertype", "onlydbo",
//		"hybrid" };
//
//	for (JsonValue questionJsonObj : jArray) {
//	    JsonObject listObj = (JsonObject) questionJsonObj;
//	    Question q = new Question();
//
//	    for (String attr : attributes) {
//		if (listObj.containsKey(attr)) {
//		    String val = listObj.get(attr).toString().replace("\"", "")
//			    .trim();
//		    q.setValue(attr, val);
//		}
//	    }
//
//	    output.add(q);
//
//	    JsonArray questionArray = listObj.getJsonArray("question");
//	    for (JsonValue questionVal : questionArray) {
//
//		JsonObject questionObj = (JsonObject) questionVal;
//
//		String lang = questionObj.getString("language");
//
//		q.languageToQuestion.put(lang, questionObj.getString("string")
//			.trim());
//
//		if (questionObj.containsKey("keywords")) {
//
//		    List<String> keywords = Arrays.asList(questionObj
//			    .getString("keywords").split(","));
//
//		    q.languageToKeywords.put(lang, keywords);
//		}
//
//	    }
//
//	    JsonObject query = (JsonObject) listObj.get("query");
//	    q.loadedAsASKQuery = false;
//	    if (query.containsKey("sparql")) {
//		String strQuery = query.getString("sparql").trim();
//		q.sparqlQuery = strQuery;
//		q.loadedAsASKQuery = (Boolean) QALD4_EvaluationUtils
//			.isAskType(strQuery);
//	    }
//	    if (query.containsKey("pseudo")) {
//		String strQuery = query.getString("pseudo").trim();
//		q.pseudoSparqlQuery = strQuery;
//		q.loadedAsASKQuery = (Boolean) QALD4_EvaluationUtils
//			.isAskType(strQuery);
//	    }
//
//	    JsonArray answerList = listObj.getJsonArray("answers");
//	    if (!answerList.isEmpty()) {
//		JsonObject answerListHead = answerList.getJsonObject(0);
//
//		JsonObject headObject = answerListHead.getJsonObject("head");
//
//		JsonArray vars = headObject.getJsonArray("vars");
//
//		HashMap<String, Set<String>> answers = new HashMap<String, Set<String>>();
//		if (!answerList.isEmpty()) {
//		    JsonObject answerObject = answerList.getJsonObject(0);
//
//		    if (answerObject.containsKey("boolean")) {
//
//			HashSet<String> ans = new HashSet<String>();
//			ans.add(answerObject.get("boolean").toString());
//			answers.put("en", ans);
//		    }
//
//		    if (answerObject.containsKey("results")) {
//
//			JsonObject resultObject = answerObject
//				.getJsonObject("results");
//
//			JsonArray bindingsList = resultObject
//				.getJsonArray("bindings");
//
//			for (JsonValue bind : bindingsList) {
//
//			    JsonObject bindObj = (JsonObject) bind;
//
//			    for (JsonValue varName : vars) {
//
//				String var = varName.toString().replaceAll(
//					"\"", "");
//				if (bindObj.containsKey(var)) {
//
//				    JsonObject j = bindObj.getJsonObject(var);
//				    HashSet<String> ans = new HashSet<String>();
//				    ans.add(j.getString("value").trim());
//				    answers.put("en", ans);
//				}
//			    }
//
//			}// end for bindingsList
//		    }// end if no result set
//
//		    q.goldenAnswers = answers;
//
//		}// end if Answerlist emptiy
//
//	    }// end For questions
//
//	}
//
//	boolean b = true;
//	for (Question k : output) {
//	    if (k.goldenAnswers.isEmpty()) {
//		if (b) {
//		    System.out
//			    .println("Following Questions (id) have no attached answers: ");
//		    b = false;
//		}
//
//		System.out.print(k.id + ", ");
//	    }
//	}
//	return output;
//    }
//
//}
