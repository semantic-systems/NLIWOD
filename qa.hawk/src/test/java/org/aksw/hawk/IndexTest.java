package org.aksw.hawk;

import org.aksw.hawk.index.DBOIndex;
import org.aksw.hawk.index.Patty_relations;
import org.aksw.qa.annotation.index.IndexDBO_classes;
import org.aksw.qa.annotation.index.IndexDBO_properties;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

public class IndexTest {
	Logger log = LoggerFactory.getLogger(IndexTest.class);
	DBOIndex dboindex = new DBOIndex();
	IndexDBO_classes classesindex = new IndexDBO_classes();
	IndexDBO_properties propertiesindex = new IndexDBO_properties();
	Patty_relations pattyindex = new Patty_relations();

	@Test
	public void dbpediaOWLTest() {
		log.info("currencies \n" + Joiner.on("\n").join(dboindex.search("currencies")));
		Assert.assertFalse(dboindex.search("currencies").contains("http://dbpedia.org/ontology/currency"));
		log.info("vice-president \n" + Joiner.on("\n").join(dboindex.search("vice-president")));
		Assert.assertTrue(dboindex.search("vice-president").contains("http://dbpedia.org/ontology/vicePresident"));
		log.info("currency \n" + Joiner.on("\n").join(dboindex.search("currency")));
		Assert.assertTrue(dboindex.search("currency").contains("http://dbpedia.org/ontology/currency"));
	}
	@Test @Ignore
	public void lemonLexiconClassesTest() {
		log.info("king \n" + Joiner.on("\n").join(classesindex.search("king")));
		Assert.assertTrue(classesindex.search("king").contains("http://dbpedia.org/ontology/Monarch"));

		log.info("pope \n" + Joiner.on("\n").join(classesindex.search("pope")));
		Assert.assertTrue(classesindex.search("pope").contains("http://dbpedia.org/ontology/Pope"));

		log.info("island \n" + Joiner.on("\n").join(classesindex.search("island")));
		Assert.assertTrue(classesindex.search("island").contains("http://dbpedia.org/ontology/Island"));

		log.info("building \n" + Joiner.on("\n").join(classesindex.search("building")));
		Assert.assertTrue(classesindex.search("building").contains("http://dbpedia.org/ontology/Building"));
	}
	@Test @Ignore
	public void lemonLexiconPropertiesTest() {
		log.info("come \n" + Joiner.on("\n").join(propertiesindex.search("come")));
		Assert.assertTrue(propertiesindex.search("come").contains("http://dbpedia.org/ontology/birthPlace"));

		log.info("composer \n" + Joiner.on("\n").join(propertiesindex.search("composer")));
		Assert.assertTrue(propertiesindex.search("composer").contains("http://dbpedia.org/ontology/musicComposer"));

		log.info("recipients \n" + Joiner.on("\n").join(propertiesindex.search("recipients")));
		Assert.assertFalse(propertiesindex.search("recipients").contains("http://dbpedia.org/ontology/award"));
		log.info("recipient \n" + Joiner.on("\n").join(propertiesindex.search("recipient")));
		Assert.assertTrue(propertiesindex.search("recipient").contains("http://dbpedia.org/ontology/award"));
	}

	@Test
	@Ignore
	// TODO if want to work
	public void YagoClassesTest() {
		log.info("street basketball player \n" + Joiner.on("\n").join(classesindex.search("street basketball player")));
		Assert.assertTrue(classesindex.search("street basketball player").contains("yago:StreetBasketballPlayers"));
		log.info("basketball player \n" + Joiner.on("\n").join(classesindex.search("basketball player")));
		Assert.assertTrue(classesindex.search("basketball player").contains("yago:StreetBasketballPlayers"));
	}

	@Test
	public void pattyrelationstest() {
		log.info("resigned \n" + Joiner.on("\n").join(pattyindex.search("resigned")));
		Assert.assertTrue(pattyindex.search("resigned").contains("http://dbpedia.org/ontology/president"));
		log.info("comes \n" + Joiner.on("\n").join(pattyindex.search("comes")));
		Assert.assertTrue(pattyindex.search("comes").contains("http://dbpedia.org/ontology/birthPlace"));
	}
}
