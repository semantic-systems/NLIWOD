package org.aksw.qa.commons.measure;

import org.junit.Assert;
import org.junit.Test;



public class QueryTypeTest {

	@Test
	public void isSelectcapitalTypeTest() {

		String queryCapitalSELECT = "prefix dbo: <http://dbpedia.org/ontology/> prefix dbr: <http://dbpedia.org/resource/> SELECT distinct * where { dbr:FIFA dbo:membership ?x . }";

		Assert.assertTrue(AnswerBasedEvaluation.isSelectType(queryCapitalSELECT));
	}

	@Test
	public void isSelectlowerTypeTest() {

		String querylowerSELECT = "prefix dbo: <http://dbpedia.org/ontology/> prefix dbr: <http://dbpedia.org/resource/> select distinct * where { dbr:FIFA dbo:membership ?x . }";
		Assert.assertTrue(AnswerBasedEvaluation.isSelectType(querylowerSELECT));
	}
}
