package org.aksw.qa.annotation.webservice;

 import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.qa.annotation.index.IndexDBO_classes;
import org.aksw.qa.annotation.index.IndexDBO_properties;
import org.aksw.qa.annotation.sparql.PatternSparqlGenerator;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.annotation.util.NifEverything;
import org.aksw.qa.annotation.util.NifEverything.NifProperty;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


 public class SparqlPatternRequestTest {
	 
	 private static final Logger LOG = LoggerFactory.getLogger(SparqlPatternRequestTest.class);
	 private NifEverything nif = NifEverything.getInstance();
	 private Spotlight spotlight = new Spotlight();
	 private IndexDBO_classes classes = new IndexDBO_classes();
	 private IndexDBO_properties properties = new IndexDBO_properties();
	 
	 @Test
	 public void sparqlPatternRequestTest() {
		 PatternSparqlGenerator sparql = PatternSparqlGenerator.getInstance();
		 String question = "Who is the spouse of Barack Obama?";
		 
		 String classAnno = getClass(question);
		 String propertyAnno = postProperty(classAnno);
		 String spotlightAnno = postSpotlight(propertyAnno);
		 
		 List<Document> nifDocs = nif.parseNIF(spotlightAnno);
		 Document nifDoc = nifDocs.get(0);
		 
		 String realAnswer = "SELECT * WHERE{  <http://dbpedia.org/resource/Barack_Obama> <http://dbpedia.org/ontology/spouse>  ?proj  .  }";
		 String answer = sparql.nifToQuery(nifDoc);
		 Assert.assertTrue(realAnswer.equals(answer));
		 LOG.debug("Final query: " + answer);
	 }
	 
	 private String postProperty(final String input) {
		 return nif.appendNIFResultFromIndexDBO(input, properties, NifProperty.TAIDENTREF);
	 }
	
	 private String getClass(final String q) {
		 return nif.createNIFResultFromIndexDBO(q, classes, NifProperty.TACLASSREF);
	 }
	
	 private String postSpotlight(final String input) {
		 return nif.appendNIFResultFromSpotters(input, spotlight);
	 }
 }
