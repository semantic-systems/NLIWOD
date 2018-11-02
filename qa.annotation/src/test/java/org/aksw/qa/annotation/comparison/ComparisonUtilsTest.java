package org.aksw.qa.annotation.comparison;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

public class ComparisonUtilsTest {

	private ComparisonUtils comp = new ComparisonUtils();
	
	@Test
	public void extractionTest() {
		String question = "What is the highest mountain in Germany? And what is the smallest mountain in Germany?";
		ArrayList<String> foundSuperlatives = comp.getSuperlatives(question);
		
		ArrayList<String> realSuperlatives = new ArrayList<String>();
		realSuperlatives.add("highest");
		realSuperlatives.add("smallest");	
		Assert.assertTrue(realSuperlatives.equals(foundSuperlatives));
		
		question = "Which countries are larger than Germany?";
		ArrayList<String> foundComparatives = comp.getComparatives(question);
		
		ArrayList<String> realComparatives = new ArrayList<String>();
		realComparatives.add("larger");
		Assert.assertTrue(realComparatives.equals(foundComparatives));
	}
	
	@Test
	public void propertiesOrderTest() {
		String adjective = "older";
		ArrayList<String> foundProperties = comp.getProperties(adjective);
		
		ArrayList<String> realProperties = new ArrayList<String>();
		realProperties.add("http://dbpedia.org/ontology/openingYear");
		realProperties.add("http://dbpedia.org/ontology/birthDate");
		Assert.assertTrue(realProperties.equals(foundProperties));
		
		String foundOrder = comp.getOrder(adjective);
		String realOrder = "DESC";
		Assert.assertTrue(realOrder.equals(foundOrder));
	}	
}
