package org.aksw.qa.commons.load.tsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.json.EJLanguage;


public class LoadTsv {
	public static final String SPLIT_KEYWORDS_ON = " ";


	public static List<IQuestion> readTSV(InputStream inputStream) throws IOException {
		// TODO Auto-generated method stub
		System.out.println("inside tsv");
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
         String line;
		Map<String, List<String>> results = new HashMap<>();
		while ((line = br.readLine()) != null) {
			String[] lineparts;
		      lineparts = line.split("\t");
			results.computeIfAbsent(lineparts[0], k -> new ArrayList<>()).add(lineparts[2]);
			
		}
		System.out.println("result set is formed");
		
		// creating map between ID and Queries testing
        File file1 = new File("/home/queries-v2.txt");
		BufferedReader br1 = new BufferedReader(new FileReader(file1));


		
		//BufferedReader br1 = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        String line1;
		Map<String, String> queries = new HashMap<>();
		while ((line1 = br1.readLine()) != null) {
			String[] lineparts;
		      lineparts = line1.split("\t");
		      queries.put(lineparts[0],(lineparts[1]));
			
		}
		List<IQuestion> DbeQuestions = new ArrayList<IQuestion>();

		for (Map.Entry<String, String> entry : queries.entrySet())
		{
		    //System.out.println(entry.getKey() + "/" + entry.getValue());
			IQuestion question = new Question();
			HashMap<String, String> langToQuestion = new HashMap<>();
			HashMap<String, List<String>> langToKeywords = new HashMap<>();
			question.setId(entry.getKey());
			question.setGoldenAnswers((Set<String>) results.get(entry.getKey()));
			
			EJLanguage lang=new EJLanguage();
			question.setId(entry.getKey());
		

			langToQuestion.put(lang.getLanguage(), entry.getValue());

			langToKeywords.put(lang.getLanguage(), Arrays.asList(entry.getValue().split(SPLIT_KEYWORDS_ON)));
			question.setLanguageToKeywords(langToKeywords);
			
			DbeQuestions.add(question);

			
			
		}
		return DbeQuestions;
}
	

}