package org.aksw.qa.commons.load.stanford;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.json.simple.parser.ParseException;

//TODO actually refactor this class to an own submodule
public class StanfordRDFizer {

	private AGDISTIS disambiguator;
	private Spotlight recognizer;

	public StanfordRDFizer() {
		this.disambiguator = new AGDISTIS();
		this.recognizer = new Spotlight();
	}

	public Map<String, List<Entity>> recognize(String question) {
		return this.recognizer.getEntities(question);
	}

	public String disambiguate(String label) {

		String preAnnotatedText = "<entity>" + label + "</entity>";

		HashMap<String, String> results;
		try {
			results = disambiguator.runDisambiguation(preAnnotatedText);
			for (String namedEntity : results.keySet()) {
				return results.get(namedEntity);
			}
		} catch (ParseException | IOException e) {
			// TODO build in proper logging
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		StanfordRDFizer stan = new StanfordRDFizer();

		// 1) NOTE! Each question has normally exactly one answer, but if
		// crowd-workers disagreed they can have multiple answers
		List<IQuestion> stanfordqa_dev = LoaderController.load(Dataset.Stanford_dev);
//		List<IQuestion> stanfordqa_train = LoaderController.load(Dataset.Stanford_train);
		for (List<IQuestion> dataset : Arrays.asList(stanfordqa_dev)) {
//		for (List<IQuestion> dataset : Arrays.asList(stanfordqa_dev, stanfordqa_train)) {
			
			// 2) run linking over all answers using AGDISTIS
			int numberOfLinkableAnswers = 0;
			for (IQuestion q : dataset) {
				String question = q.getLanguageToQuestion().get("en");
				if (q.getGoldenAnswers().size() == 1) {
					for (String answer : q.getGoldenAnswers()) {
						String disambiguate = stan.disambiguate(answer);
						if (disambiguate != null) {
							System.out.println("Question: " + question);
							numberOfLinkableAnswers++;
							System.out.println("\tDisambiguated Answer: " + answer + " -> " + disambiguate);

							// 3) run NER+NED over all questions, according to
							// experiments, we use Spotlight
							stan.recognize(question).forEach((x, y) -> System.out.println("\tRecognized Entities in Question: " + x + " -> " + y));

							// 4) for queries with a resource as answer run QTL

							// 5) run best QTL against DBpedia and measure
							// f-measure/accuracy to answer
						}
					}
				}

			}
			System.out.println("Number Of Linkable Answers " + numberOfLinkableAnswers);
		}
	}
}
