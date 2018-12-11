package org.aksw.qa.annotation.sparql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.qa.annotation.spotter.SpotterTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PatternSparqlGeneratorTest {
	
	private static final String EMPTY = "No pattern for those quantities of classes / properties / named entities available";
	
	private static Logger LOG = LoggerFactory.getLogger(SpotterTest.class);
	
	private static PatternSparqlGenerator gen = PatternSparqlGenerator.getInstance();
	
	@Test
	public void notEmptyTest() {
		 List<String> cStr = new ArrayList<>(Arrays.asList("class1", "class2", "dummy"));
		 List<String> pStr = new ArrayList<>(Arrays.asList("property1", "property2", "dummy"));
		 List<String> nStr = new ArrayList<>(Arrays.asList("namedEntity1", "namedEntity2", "dummy"));
		
		 List<String> classes = new ArrayList<>();
		 List<String> properties = new ArrayList<>();
		 List<String> namedEntities = new ArrayList<>();
		 
		 for (int i = 0; i < 3; i++) {
			 for (int j = 0; j < 3; j++) {
				 for (int k = 0; k < 3; k++) {
					 LOG.debug("Constructing for " + i + " Classes, " + j + " properties, " + k + " namedEntitites\n");
					 String query = gen.generateQuery(classes, properties, namedEntities);
					 LOG.debug("Generated query:" + query);
					 LOG.debug("\r\n");
					 
					 //if there are no classes, properties, and named entities there should be no pattern
					 if(i == 0 && j == 0 && k == 0) {
						 Assert.assertTrue(EMPTY.equals(query));
					 } else {
						 Assert.assertTrue(!EMPTY.equals(query));
					 }
					 namedEntities.add(nStr.get(0));
					 nStr.remove(0);
				 }
				 nStr.addAll(namedEntities);
				 namedEntities.clear();
				
				 properties.add(pStr.get(0));
				 pStr.remove(0);
			 }
			 pStr.addAll(properties);
			 properties.clear();
			
			 classes.add(cStr.get(0));
			 cStr.remove(0);
		 }
	}
	
	@Test
	public void specificQuantitiesTest() {
		 List<String> cStr = new ArrayList<>(Arrays.asList("class1", "class2"));
		 List<String> pStr = new ArrayList<>(Arrays.asList("property1"));
		 List<String> nStr = new ArrayList<>(Arrays.asList("namedEntity1"));
		 
		 LOG.debug("Constructing for " + cStr.size() + " Classes, " + pStr.size() + " properties, " + nStr.size() + " namedEntitites\n");
		 String query = gen.generateQuery(cStr, pStr, nStr);
		 String correctAnswer = "SELECT * WHERE{   ?proj  a dbo:class1 .   ?proj  a dbo:class2 .  dbr:namedEntity1 dbo:property1  ?proj  .  }";
		 Assert.assertTrue(correctAnswer.equals(query));
		 LOG.debug("Generated query:" + query);
		 
		 cStr.clear();
		 
		 LOG.debug("Constructing for " + cStr.size() + " Classes, " + pStr.size() + " properties, " + nStr.size() + " namedEntitites\n");
		 query = gen.generateQuery(cStr, pStr, nStr);
		 correctAnswer = "SELECT * WHERE{  dbr:namedEntity1 dbo:property1  ?proj  .  }" ;
		 Assert.assertTrue(correctAnswer.equals(query));
		 LOG.debug("Generated query:" + query);
	}
}