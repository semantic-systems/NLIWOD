package org.aksw.hawk.experiment;

import java.util.List;
import java.util.Map;

import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.nlp.spotter.Spotlight;
import org.aksw.hawk.nlp.spotter.TagMe;
import org.aksw.hawk.nlp.spotter.WikipediaMiner;

public class AllSpotterOnExcerpt {

	public static void main(String args[]) {

		String excerpt = "In the final years of his life, King expanded his focus to include poverty and the Vietnam War, alienating many of his liberal allies with a 1967 speech titled \"Beyond Vietnam\". King was planning a national occupation of Washington, D.C. , called the Poor People's Campaign. King was assassinated on April 4, 1968, in Memphis, Tennessee. His death was followed by riots in many U.S. cities. Allegations that James Earl Ray, the man convicted of killing King, had been framed or acted in concert with government agents persisted for decades after the shooting, and the jury of a 1999 civil trial found Loyd Jowers to be complicit in a conspiracy against King.";

		for (ASpotter nerdModule : new ASpotter[] { new Spotlight(), new Fox(), new TagMe(), new WikipediaMiner() }) {
			Map<String, List<Entity>> nes = nerdModule.getEntities(excerpt);

			if (!nes.isEmpty()) {
				for (Entity ent : nes.get("en")) {
					System.out.println("\t" + nerdModule.toString() + "\t" + ent.toString());
				}
			}
		}
		
		  excerpt = "In which city was the assassin of Martin Luther King born?";

		for (ASpotter nerdModule : new ASpotter[] { new Spotlight(), new Fox(), new TagMe(), new WikipediaMiner() }) {
			Map<String, List<Entity>> nes = nerdModule.getEntities(excerpt);

			if (!nes.isEmpty()) {
				for (Entity ent : nes.get("en")) {
					System.out.println("\t" + nerdModule.toString() + "\t" + ent.toString());
				}
			}
		}
	}
}