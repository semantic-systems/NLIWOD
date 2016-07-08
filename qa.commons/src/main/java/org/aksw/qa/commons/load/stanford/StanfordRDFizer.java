package org.aksw.qa.commons.load.stanford;

import java.io.BufferedWriter;
import java.io.FileWriter;
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

	public static void main(String[] args) throws IOException {
		StanfordRDFizer stan = new StanfordRDFizer();

		// 1) NOTE! Each question has normally exactly one answer, but if
		// crowd-workers disagreed they can have multiple answers
		List<IQuestion> stanfordqa_dev = LoaderController.load(Dataset.Stanford_train);
		// List<IQuestion> stanfordqa_train =
		// LoaderController.load(Dataset.Stanford_train);

		BufferedWriter bw = new BufferedWriter(new FileWriter("Stanford_train.tsv"));
		for (List<IQuestion> dataset : Arrays.asList(stanfordqa_dev)) {
			// for (List<IQuestion> dataset : Arrays.asList(stanfordqa_dev,
			// stanfordqa_train)) {

			// 2) run linking over all answers using AGDISTIS
			int numberOfLinkableAnswers = 0;
			int numberOfQuestions = 0;
			for (IQuestion q : dataset) {
				String question = q.getLanguageToQuestion().get("en");
				// if (q.getGoldenAnswers().size() == 1) {
				for (String answer : q.getGoldenAnswers()) {
					String disambiguate = stan.disambiguate(answer);
					System.out.println(numberOfQuestions + ". Question: " + question);
					numberOfQuestions++;
					bw.write(question + "\t");
					if (disambiguate != null) {
						numberOfLinkableAnswers++;
						System.out.println("\tDisambiguated Answer: " + answer + " -> " + disambiguate);
						bw.write(answer + "\t");
						bw.write(disambiguate + "\t");
					} else {
						bw.write(answer + "\t");
						bw.write("null" + "\t");
					}
					// 3) run NER+NED over all questions, according to
					// experiments, we use Spotlight
					System.out.print("\tRecognized Entities in Question: ");
					Map<String, List<Entity>> recognize = stan.recognize(question);
					if (!recognize.isEmpty()) {
						recognize.get("en").forEach(x -> {
							try {
								bw.write(x.uris.get(0) + "\t");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							System.out.print(x.uris.get(0) + "\t");
						});
						// 4) for queries with a resource as answer run QTL

						// 5) run best QTL against DBpedia and measure
						// f-measure/accuracy to answer
					}
					System.out.println();
					bw.newLine();
				}
				// }
				if (numberOfQuestions % 50 == 0) {
					bw.flush();
				}
			}
			bw.close();
			System.out.println("Number Of Linkable Answers " + numberOfLinkableAnswers);
		}
	}
}
