package org.aksw.qa.commons.load.stanford;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBpediaIndexTest {
	private Logger log = LoggerFactory.getLogger(DBpediaIndexTest.class);
	
	@Test
	 public void testIndex()  {
		DBpediaIndex dboindex = new DBpediaIndex();
		log.debug("Searching for: http://dbpedia.org/ontology/currency in currencies");
		Assert.assertFalse(dboindex.search("currencies").contains("http://dbpedia.org/ontology/currency"));		
		log.debug(dboindex.search("currencies").toString());
		
		log.debug("Searching for: http://dbpedia.org/ontology/vicePresident in vice-president");
		Assert.assertTrue(dboindex.search("vice-president").contains("http://dbpedia.org/ontology/vicePresident"));
		log.debug(dboindex.search("vice-president").toString());
		
		log.debug("Searching for: http://dbpedia.org/ontology/currency in currency");
		Assert.assertTrue(dboindex.search("currency").contains("http://dbpedia.org/ontology/currency"));
		log.debug(dboindex.search("currency").toString());
	 }
}
