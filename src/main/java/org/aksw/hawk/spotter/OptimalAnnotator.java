package org.aksw.hawk.spotter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class OptimalAnnotator extends ASpotter {
	static Logger log = LoggerFactory.getLogger(OptimalAnnotator.class);
	private Map<String, String> map = Maps.newHashMap();

	public OptimalAnnotator() {
		try {
			BufferedReader br = new BufferedReader(new FileReader("resources/qald-4_hybrid_train-gold_ner.tsv"));
			while (br.ready()) {
				String line = br.readLine();
				String split[] = line.split("\t");
				map.put(split[1], split[3]);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Map<String, List<Entity>> getEntities(final String question) {
		HashMap<String, List<Entity>> tmp = new HashMap<>();
		ArrayList<Entity> tmpList = new ArrayList<>();

		Entity ent = new Entity();
		ent.label = question;
		ent.uris.add(new ResourceImpl(map.get(question)));
		tmpList.add(ent);
		tmp.put("en", tmpList);

		return tmp;
	}

	public static void main(final String args[]) {
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "Which buildings in art deco style did Shreve, Lamb and Harmon design?");
		ASpotter spotter = new OptimalAnnotator();
		q.setLanguageToNamedEntites(spotter.getEntities(q.getLanguageToQuestion().get("en")));
		for (String key : q.getLanguageToNamedEntites().keySet()) {
			System.out.println(key);
			for (Entity entity : q.getLanguageToNamedEntites().get(key)) {
				System.out.println("\t" + entity.label + " ->" + entity.type);
				for (Resource r : entity.posTypesAndCategories) {
					System.out.println("\t\tpos: " + r);
				}
				for (Resource r : entity.uris) {
					System.out.println("\t\turi: " + r);
				}
			}
		}
	}
}
