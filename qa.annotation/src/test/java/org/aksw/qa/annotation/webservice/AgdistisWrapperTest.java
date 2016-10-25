package org.aksw.qa.annotation.webservice;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.gerbil.transfer.nif.data.NamedEntity;
import org.aksw.qa.annotation.util.NifEverything;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AgdistisWrapperTest {
	private Document testDoc;
	private AgdistisWrapper agdistis;

	@Before
	public void createDoc() {
		agdistis = new AgdistisWrapper();
		String q = "Who are the successors of Barack Obama and Michelle Obama?";
		String namedEntity1 = "Barack Obama";
		String namedEntity2 = "Michelle Obama";
		Document doc = new DocumentImpl(q);
		NamedEntity obama = new NamedEntity(q.indexOf(namedEntity1), namedEntity1.length(), "someUri", true);
		NamedEntity michelle = new NamedEntity(q.indexOf(namedEntity2), namedEntity2.length(), "someUri2", true);
		doc.addMarking(obama);
		doc.addMarking(michelle);
		testDoc = doc;
	}

	@Test
	public void createAgdistisString() {

		String qEntity = "Who are the successors of <entity>Barack Obama</entity> and <entity>Michelle Obama</entity>?";
		String processed = agdistis.createAgdistisString(testDoc);
		Assert.assertTrue("Should be: \n" + qEntity + "\n but was\n" + processed, qEntity.equals(processed));
	}

	@Test
	public void testAgdistis() {
		String testDocNif = NifEverything.getInstance().writeNIF(testDoc);
		System.out.println(testDocNif);
		String q = testDoc.getText();
		Document doc = new DocumentImpl(q);
		String namedEntity1 = "Barack Obama";
		String namedEntity2 = "Michelle Obama";
		NamedEntity obama = new NamedEntity(q.indexOf(namedEntity1), namedEntity1.length(), "http://dbpedia.org/resource/Barack_Obama");
		NamedEntity michelle = new NamedEntity(q.indexOf(namedEntity2), namedEntity2.length(), "http://dbpedia.org/resource/Michelle_Obama");
		doc.addMarking(obama);
		doc.addMarking(michelle);
		String shouldBeNif = NifEverything.getInstance().writeNIF(doc);
		Assert.assertTrue("Should be: \n" + shouldBeNif + "\n but was\n" + testDocNif, shouldBeNif.equals(agdistis.process(testDocNif)));
	}

	@Test
	public void emptyTest() {
		agdistis.process(null);
		agdistis.process("");

	}

}
