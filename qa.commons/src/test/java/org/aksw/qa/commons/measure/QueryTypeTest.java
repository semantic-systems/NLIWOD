package org.aksw.qa.commons.measure;

import org.junit.Test;

import junit.framework.Assert;

public class QueryTypeTest {
	@SuppressWarnings("deprecation")
	@Test
	public void isSelectcapitalTypeTest() {

		String queryCapitalSELECT = "prefix dbo: <http://dbpedia.org/ontology/> prefix dbr: <http://dbpedia.org/resource/> SELECT distinct * where { dbr:FIFA dbo:membership ?x . }";
		Assert.assertTrue(AnswerBasedEvaluation.isSelectType(queryCapitalSELECT));
	}
	@SuppressWarnings("deprecation")
	@Test
	public void isSelectlowerTypeTest() {

		String querylowerSELECT = "prefix dbo: <http://dbpedia.org/ontology/> prefix dbr: <http://dbpedia.org/resource/> select distinct * where { dbr:FIFA dbo:membership ?x . }";
		Assert.assertTrue(AnswerBasedEvaluation.isSelectType(querylowerSELECT));
	}
}
