package hawk;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.hawk.spotter.ASpotter;
import org.aksw.hawk.spotter.Fox;
import org.aksw.hawk.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpotterTest {
	Logger log = LoggerFactory.getLogger(SpotterTest.class);

	@Test
	public void emtpyTest() {
		Set<RDFNode> systemAnswers = new HashSet<>();
		systemAnswers.add(new ResourceImpl("true"));
		for (ASpotter m : new ASpotter[] { new Fox(), new Spotlight() }) {
			log.info(m.toString());
			Map<String, List<Entity>> ents = m.getEntities("Where did the first man in space die?");
			if (!ents.isEmpty()) {
				for (Entity ent : ents.get("en")) {
					log.debug("\t" + ent.toString());
				}
			}
		}
	}

}
