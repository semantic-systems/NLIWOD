package org.aksw.qa.commons.load.tsv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.json.EJLanguage;

/**
 * Loads TSV files
 *
 * @author suganya31
 */

public class LoadTsv {
	public static final String SPLIT_KEYWORDS_ON = " ";

	public static List<IQuestion> readTSV(InputStream queriesStream, InputStream qrels, String data)
			throws IOException {
		String line;
		List<IQuestion> DbeQuestions = new ArrayList<IQuestion>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(qrels, "UTF-8"));
		Map<String, Set<String>> results = new HashMap<>();
		Map<String, String> queries = new HashMap<>();

		while ((line = reader.readLine()) != null) {
			String[] lineparts;
			if (line.startsWith(data)) {
				lineparts = line.split("\t");
				String result = lineparts[2].replaceAll("<dbpedia:", "<http://dbpedia.org/resource/");
				results.computeIfAbsent(lineparts[0], k -> new HashSet<>()).add(result);
			}

		}
		reader.close();

		reader = new BufferedReader(new InputStreamReader(queriesStream, "UTF-8"));

		while ((line = reader.readLine()) != null) {
			String[] lineparts;
			lineparts = line.split("\t");
			if (line.startsWith(data)) {
				queries.put(lineparts[0], (lineparts[1]));
			}
		}
		reader.close();

		for (Map.Entry<String, String> entry : queries.entrySet()) {
			IQuestion question = new Question();
			HashMap<String, String> langToQuestion = new HashMap<>();
			HashMap<String, List<String>> langToKeywords = new HashMap<>();
			question.setId(entry.getKey());
			question.setGoldenAnswers(results.get(entry.getKey()));
			EJLanguage lang = new EJLanguage();
			question.setId(entry.getKey());
			langToQuestion.put(lang.getLanguage(), entry.getValue());
			langToKeywords.put(lang.getLanguage(), Arrays.asList(entry.getValue().split(SPLIT_KEYWORDS_ON)));
			question.setLanguageToKeywords(langToKeywords);
			question.setLanguageToQuestion(langToQuestion);

			DbeQuestions.add(question);

		}
		return DbeQuestions;
	}

}