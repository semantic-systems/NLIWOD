package org.aksw.hawk.experiment;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.index.Patty_relations;
import org.aksw.qa.commons.load.QALD_Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*
 * EXPERIMENTAL! 
 */
public class IndexComparer {
/*
 * TODO: compare generated queries of all index classes somehow.
 */
	static Logger log = LoggerFactory.getLogger(IndexComparer.class);
	static Patty_relations pattyindex = new Patty_relations();

	public static void main(String[] args) {
		List<HAWKQuestion> questions = QALDQuestions();
		List<String> languagequestions = new ArrayList<String>();
		questions.forEach(x -> languagequestions.add(x.getLanguageToQuestion().get("en").replaceAll("\\p{P}", "")));
		
		//Test one question
		List<String> uriset = new ArrayList<String>();
		for(int i = 0; i < languagequestions.get(10).split(" ").length; i++){
		uriset.addAll(pattyindex.search(languagequestions.get(10).split(" ")[i]));
		}
		//uriset.forEach(x -> System.out.println(x));
		uriset.removeIf(x -> Collections.frequency(uriset, x) < 1);
		uriset.forEach(x -> System.out.println(x));
		System.out.println(languagequestions.get(10));
	}
/*
 * Get Questions from QALD6-Data
 */
	private static List<HAWKQuestion> QALDQuestions(){
		log.info("Loading dataset");
		URL url = ClassLoader.getSystemClassLoader().getResource("QALD-6/qald-6-train-multilingual.json");
		System.out.println(url);
		List<HAWKQuestion> questions = null;
		try {
			questions = HAWKQuestionFactory.createInstances(QALD_Loader.loadJSON(url.openStream()));

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
		return questions;
	}
/*
 * TODO: Metod to turn natural-language question to list of uri's.
 */
}
