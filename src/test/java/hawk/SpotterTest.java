package hawk;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.nlp.spotter.NERD_module;
import org.aksw.hawk.nlp.spotter.Spotlight;
import org.aksw.hawk.nlp.spotter.WikipediaMiner;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class SpotterTest {
	Logger log = LoggerFactory.getLogger(SpotterTest.class);

	@Test
	public void emtpyTest() {
		Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
		systemAnswers.add(new ResourceImpl("true"));
		for (NERD_module m : new NERD_module[] { new Fox(), new Spotlight(), new WikipediaMiner() }) {
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
