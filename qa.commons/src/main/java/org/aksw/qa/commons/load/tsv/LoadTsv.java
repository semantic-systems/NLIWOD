package org.aksw.qa.commons.load.tsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.json.EJLanguage;


public class LoadTsv {
	public static final String SPLIT_KEYWORDS_ON = " ";


	public static List<IQuestion> readTSV(InputStream is, String data) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("inside tsv");
        String line;

		File file1 = new File("/home/suganya/queries-v2.txt");

		//   File file = new File("/home/suganya/qrels-v2.txt");

	//Creating map between ID and results
	       //BufferedReader br = new BufferedReader(new FileReader(file));
	        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));


			Map<String, Set<String>> results = new HashMap<>();
			while ((line = br.readLine()) != null) {
				String[] lineparts;
				if(line.startsWith(data))
				{
			      lineparts = line.split("\t");
			      String items =lineparts[2].replaceAll("<dbpedia:", "<http://dbpedia.org/resource/");
			       results.computeIfAbsent(lineparts[0], k -> new HashSet<>()).add(items);
				}
				
			}
			//System.out.println(results);
			
			// creating map between ID and Queries
			
		BufferedReader br1 = new BufferedReader(new FileReader(file1));
	    //    BufferedReader br1 = new BufferedReader(new InputStreamReader(getLoadingAnchor().getResourceAsStream("/queries-v2.txt"), "UTF-8"));

	        String line1;
			Map<String, String> queries = new HashMap<>();
			while ((line1 = br1.readLine()) != null) {
				String[] lineparts;
			      lineparts = line1.split("\t");
			      if(line1.startsWith(data))
			      {
			      queries.put(lineparts[0],(lineparts[1]));
			      }
			}
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
            question.setLanguageToQuestion(langToQuestion);

		

			langToQuestion.put(lang.getLanguage(), entry.getValue());

			langToKeywords.put(lang.getLanguage(), Arrays.asList(entry.getValue().split(SPLIT_KEYWORDS_ON)));
			question.setLanguageToKeywords(langToKeywords);
			
			DbeQuestions.add(question);

			
			
		}
		return DbeQuestions;
}
	

}