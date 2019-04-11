package org.aksw.qa.annotation.spotter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotterTest {
	private Logger log = LoggerFactory.getLogger(SpotterTest.class);
	
	@Ignore
	@Test
	public void emtpyTest() {
		Set<RDFNode> systemAnswers = new HashSet<>();
		systemAnswers.add(new ResourceImpl("true"));
		for (ASpotter m : new ASpotter[] { new Fox()}) {
			log.debug(m.toString());
			Map<String, List<Entity>> ents = m.getEntities("Angela Merkel visits Berlin.");
			System.out.println(ents);
			if (!ents.isEmpty()) {
				for (Entity ent : ents.get("en")) {
					log.debug("\t" + ent.toString());
				}
			}
		}
	}
	
	@Ignore
	@Test
	public void offsetTest() {
		for (ASpotter m : new ASpotter[] { new Fox()}) {
			Map<String, List<Entity>> ents = m.getEntities("Who was the wife of U.S. president Lincoln?");
			System.out.println(ents);
			for (Entity ent : ents.get("en")) {
				log.debug(m.toString());
				Assert.assertTrue(ent.getOffset() > 0);
				log.debug(ent + " | " + "Offset: " + ent.getOffset());
			}
		}
	}
}
