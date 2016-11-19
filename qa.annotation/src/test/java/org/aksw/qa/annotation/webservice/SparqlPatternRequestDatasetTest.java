package org.aksw.qa.annotation.webservice;
// package org.aksw.qa.annotation.webservice;
//
// import java.io.BufferedWriter;
// import java.io.File;
// import java.io.FileWriter;
// import java.io.IOException;
// import java.util.ArrayList;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Set;
//
// import org.aksw.gerbil.transfer.nif.Document;
// import org.aksw.qa.annotation.index.IndexDBO_classes;
// import org.aksw.qa.annotation.index.IndexDBO_properties;
// import org.aksw.qa.annotation.sparql.PatternSparqlGenerator;
// import org.aksw.qa.annotation.spotter.Spotlight;
// import org.aksw.qa.annotation.util.NifEverything;
// import org.aksw.qa.annotation.util.NifEverything.NifProperty;
// import org.aksw.qa.commons.datastructure.IQuestion;
// import org.aksw.qa.commons.load.Dataset;
// import org.aksw.qa.commons.load.LoaderController;
//
// TODO jonathan
// public class DatasetTest {
//
// private NifEverything nif = NifEverything.getInstance();
// private Spotlight spotlight = new Spotlight();
// private IndexDBO_classes classes = new IndexDBO_classes();
// private IndexDBO_properties properties = new IndexDBO_properties();
// private PatternSparqlGenerator sparql = PatternSparqlGenerator.getInstance();
//
// public String postProperty(final String input) {
// return nif.appendNIFResultFromIndexDBO(input, properties,
// NifProperty.TAIDENTREF);
// }
//
// public String getClass(final String q) {
// return nif.createNIFResultFromIndexDBO(q, classes, NifProperty.TACLASSREF);
// }
//
// public String postSpotlight(final String input) {
//
// return nif.appendNIFResultFromSpotters(input, spotlight);
// }
//
// public void createNifs(final Dataset dataset) {
// List<String> nifs = new ArrayList<>();
// List<IQuestion> questions = LoaderController.load(dataset);
// Set<String> errorQuestions = new HashSet<>();
// for (IQuestion it : questions) {
// if (!it.getLanguageToQuestion().get("en").equals("Give me all members of
// Prodigy.")) {
// // continue;
// }
//
// String classAnno = getClass(it.getLanguageToQuestion().get("en"));
// String propertyAnno = postProperty(classAnno);
// String spotlightAnno = postSpotlight(propertyAnno);
// nifs.add(spotlightAnno);
// List<Document> nifDocs = nif.parseNIF(spotlightAnno);
// Document nifDoc = nifDocs.get(0);
//
// errorQuestions.add(it.getLanguageToQuestion().get("en") + "\r\n\r\n" +
// sparql.nifToQuery(nifDoc));
//
// // for (Marking marking : nifDoc.getMarkings()) {
// // errorQuestions.add(marking.getClass().getSimpleName());
// // }
//
// }
// StringBuilder build = new StringBuilder();
// for (String it : errorQuestions) {
//
// build.append(it);
// build.append("\r\n\r\n\r\n''''''''''''''''''''''''''''''''''''''''''''''''''''''''''\r\n\r\n\r\n");
// }
// File f = new File("c:/output/sparqlNifsFromQald.txt");
//
// try {
// BufferedWriter writer = new BufferedWriter(new FileWriter(f));
// writer.write(build.toString());
// } catch (IOException e) {
// // TODO Auto-generated catch block
// e.printStackTrace();
// }
// for (String s : errorQuestions) {
// System.out.println(s);
// }
// System.out.println("Done");
//
// }
//
// public static void main(final String[] args) {
//
// new DatasetTest().createNifs(Dataset.QALD6_Train_Multilingual);
//
// }
// }
