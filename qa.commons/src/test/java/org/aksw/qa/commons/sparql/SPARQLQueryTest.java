package org.aksw.qa.commons.sparql;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class SPARQLQueryTest {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void queryGeneratorTest() {
		SPARQLQuery query = new SPARQLQuery();
		query.addConstraint("?proj a <http://dbpedia.org/ontology/Person>.");
		query.addFilterOverAbstractsContraint("?prof", "Mandela anti-apartheid activist");
		
		String realAnswer1 = "PREFIX text:<http://jena.apache.org/text#> \n" + 
				"SELECT DISTINCT ?proj WHERE {\n" + 
				"?prof text:query (<http://dbpedia.org/ontology/abstract> '\"Mandela anti-apartheid activist\"' 1000). \n" + 
				"?proj a <http://dbpedia.org/ontology/Person>. \n" + 
				"}\n" + 
				"LIMIT 1";
		
		String realAnswer2 = "PREFIX text:<http://jena.apache.org/text#> \n" + 
				"SELECT DISTINCT ?proj WHERE {\n" + 
				"?prof text:query (<http://dbpedia.org/ontology/abstract> 'activist~1 AND Mandela~1 AND apartheid~1 AND anti~1' 1000). \n" + 
				"?proj a <http://dbpedia.org/ontology/Person>. \n" + 
				"}\n" + 
				"LIMIT 1";
		
		ArrayList<String> answers = Lists.newArrayList(query.generateQueries());
		
		Assert.assertTrue("Answer 1 differs.", realAnswer1.equals(answers.get(0)));
		log.debug("First answer: \n" + answers.get(0));
		Assert.assertTrue("Answer 2 differs.", realAnswer2.equals(answers.get(1)));
		log.debug("Second answer: \n" + answers.get(1));
	}
	
	@Test
	public void cloneTest() throws CloneNotSupportedException {
		SPARQLQuery query = new SPARQLQuery();
		query.addConstraint("?proj a <http://dbpedia.org/ontology/Person>.");
		query.addFilterOverAbstractsContraint("?prof", "Mandela anti-apartheid activist");
		
		String realAnswer = "PREFIX text:<http://jena.apache.org/text#> \n" + 
				"SELECT DISTINCT ?proj WHERE {\n" + 
				"?prof text:query (<http://dbpedia.org/ontology/abstract> '\"Mandela anti-apartheid activist\"' 1000). \n" + 
				"?proj a <http://dbpedia.org/ontology/Person>. \n" + 
				"}\n" + 
				"LIMIT 1";
		
		SPARQLQuery answer = (SPARQLQuery) query.clone();
		Assert.assertTrue("Clone differs from real answer.", realAnswer.equals(answer.toString()));
	}
}
