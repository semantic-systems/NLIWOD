package org.aksw.qa.commons.load.tsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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

public class Testing {
	public static  List<IQuestion> function() throws IOException
	{
		final String SPLIT_KEYWORDS_ON = " ";
		System.out.println("inside function");


		File file1 = new File("/home/suganya/queries-sample.txt");

		   File file = new File("/home/suganya/qrels-sample.txt");

	//Creating map between ID and results
	        BufferedReader br = new BufferedReader(new FileReader(file));
	         String line;
				
			Map<String, Set<String>> results = new HashMap<>();
			while ((line = br.readLine()) != null) {
				String[] lineparts;
			      lineparts = line.split("\t");
			      String items =lineparts[2].replaceAll("<dbpedia:", "<http://dbpedia.org/resource/");

				results.computeIfAbsent(lineparts[0], k -> new HashSet<>()).add(items);
				
			}
			//System.out.println(results);
			
			// creating map between ID and Queries
			
			BufferedReader br1 = new BufferedReader(new FileReader(file1));
	        String line1;
			Map<String, String> queries = new HashMap<>();
			while ((line1 = br1.readLine()) != null) {
				String[] lineparts;
			      lineparts = line1.split("\t");
			      queries.put(lineparts[0],(lineparts[1]));
				
			}
			System.out.println(queries);
			List<IQuestion> DbeQuestions = new ArrayList<IQuestion>();

			for (Map.Entry<String, String> entry : queries.entrySet())
			{
			    //System.out.println(entry.getKey() + "/" + entry.getValue());
				IQuestion question = new Question();
				HashMap<String, String> langToQuestion = new HashMap<>();
				HashMap<String, List<String>> langToKeywords = new HashMap<>();
				question.setId(entry.getKey());
				question.setGoldenAnswers(results.get(entry.getKey()));
				
				EJLanguage lang=new EJLanguage();
				question.setId(entry.getKey());
			

				langToQuestion.put(lang.getLanguage(), entry.getValue());

				langToKeywords.put(lang.getLanguage(), Arrays.asList(entry.getValue().split(SPLIT_KEYWORDS_ON)));
				question.setLanguageToKeywords(langToKeywords);
				question.setLanguageToQuestion(langToQuestion);
				
				DbeQuestions.add(question);
				
				System.out.println(DbeQuestions);

				
				
			}
			return DbeQuestions;
	


}

}
