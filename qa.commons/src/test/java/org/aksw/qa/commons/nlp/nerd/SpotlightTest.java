package org.aksw.qa.commons.nlp.nerd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotlightTest {
	private Logger log = LoggerFactory.getLogger(SpotlightTest.class);
	
	private ArrayList<Entity> realEntities = new ArrayList<Entity>();
	
	@Before
	public void setUpEntities() {
		Entity e1 = new Entity("atomic weapons","");
		e1.getUris().add(new ResourceImpl("http://dbpedia.org/resource/Nuclear_weapon"));
		realEntities.add(e1);
		
		Entity e2 = new Entity("Japan","");
		e2.getUris().add(new ResourceImpl("http://dbpedia.org/resource/Japan"));
		realEntities.add(e2);
				
		Entity e3 = new Entity("World War II","");
		e3.getUris().add(new ResourceImpl("http://dbpedia.org/resource/World_War_II"));
		realEntities.add(e3);
	}
	
	@Test
	public void spotlightTest() throws ParseException, IOException {		
		String input = "Who was vice president under the president who approved the use of atomic weapons against Japan during World War II?";
		Spotlight spotter = new Spotlight();
		spotter.setConfidence(0.6);
		log.debug(("Confidence: " + spotter.getConfidence()));
		Map<String, List<Entity>> entities = spotter.getEntities(input);
		List<Entity> foundEntities = entities.get("en");
		for(int i = 0; i<foundEntities.size(); i++) {
			log.debug(foundEntities.get(i).toString());
			Assert.assertTrue(foundEntities.get(i).equals(realEntities.get(i)));
		}
	}
}
