package org.aksw.qa.annotation.spotter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.Test;

public class SpotterTest {

	@Test
	public void emtpyTest() {
		Set<RDFNode> systemAnswers = new HashSet<>();
		systemAnswers.add(new ResourceImpl("true"));
		for (ASpotter m : new ASpotter[] { new Spotlight()}) {
//			for (ASpotter m : new ASpotter[] { new Spotlight(), new Fox()}) {

			System.out.println(m.toString());
			Map<String, List<Entity>> ents = m.getEntities("Angela Merkel visits Berlin.");
			if (!ents.isEmpty()) {
				for (Entity ent : ents.get("en")) {
					System.out.println("\t" + ent.toString());
				}
			}
		}
	}

}
