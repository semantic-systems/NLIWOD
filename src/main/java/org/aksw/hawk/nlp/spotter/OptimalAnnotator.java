package org.aksw.hawk.nlp.spotter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.datastructures.Question;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

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
	public Map<String, List<Entity>> getEntities(String question) {
		HashMap<String, List<Entity>> tmp = new HashMap<String, List<Entity>>();
		ArrayList<Entity> tmpList = new ArrayList<>();

		Entity ent = new Entity();
		ent.label = question;
		ent.uris.add(new ResourceImpl(map.get(question)));
		tmpList.add(ent);
		tmp.put("en", tmpList);

		return tmp;
	}

	public static void main(String args[]) {
		Question q = new Question();
		q.languageToQuestion.put("en", "Which buildings in art deco style did Shreve, Lamb and Harmon design?");
		ASpotter spotter = new OptimalAnnotator();
		q.languageToNamedEntites = spotter.getEntities(q.languageToQuestion.get("en"));
		for (String key : q.languageToNamedEntites.keySet()) {
			System.out.println(key);
			for (Entity entity : q.languageToNamedEntites.get(key)) {
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
