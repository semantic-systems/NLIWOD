package org.aksw.qa.commons.load.tsv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.load.json.EJLanguage;
import org.aksw.qa.commons.load.json.EJQuestionFactory;
import org.aksw.qa.commons.load.json.ExtendedQALDJSONLoader;
import org.aksw.qa.commons.load.json.QaldJson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;




public class Testing1 {

	public static final String SPLIT_KEYWORDS_ON = " ";
	public static void main(String[] args)  {
		// TODO Auto-generated method stub
		String data="SemSearch";
		
        
			
		try {
			function(data);
			//function1();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	
	public static  List<IQuestion> function(String dataSet) throws IOException
	{

		File file1 = new File("/home/suganya/queries-v2.txt");

		   File file = new File("/home/suganya/qrels-v2.txt");

	//Creating map between ID and results
	        BufferedReader br = new BufferedReader(new FileReader(file));
	         String line;
			Map<String, Set<String>> results = new HashMap<>();
			while ((line = br.readLine()) != null) {
				/*if(!line.startsWith("SemSearch"))
				{	System.out.println("inside sem");

					break;
					
				}*/
				String[] lineparts;
				if(line.startsWith(dataSet))
				{
			      lineparts = line.split("\t");
				
			      String items =lineparts[2].replaceAll("<dbpedia:", "<http://dbpedia.org/resource/");
			  //    System.out.println(items);
			    //  System.out.println(lineparts[2]);
			     

				results.computeIfAbsent(lineparts[0], k -> new HashSet<>()).add(items);
				}
				
			}
			//System.out.println(results);
			
			// creating map between ID and Queries
			
			BufferedReader br1 = new BufferedReader(new FileReader(file1));
	        String line1;
			Map<String, String> queries = new HashMap<>();
			while ((line1 = br1.readLine()) != null) {
				String[] lineparts;
			      lineparts = line1.split("\t");
			      if(line1.startsWith(dataSet))
			      {
			      queries.put(lineparts[0],(lineparts[1]));
			      }
				
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
				//System.out.println(results.get(entry.getKey()));
				question.setGoldenAnswers(results.get(entry.getKey()));
				String sparql="SELECT DISTINCT ?uri WHERE { <http://dbpedia.org/resource/Gestapo> <http://dbpedia.org/ontology/parentOrganisation> ?x . ?x <http://dbpedia.org/ontology/leader> ?uri  . }";
				
				EJLanguage lang=new EJLanguage();
				question.setId(entry.getKey());
			

				langToQuestion.put(lang.getLanguage(), entry.getValue());

				langToKeywords.put(lang.getLanguage(), Arrays.asList(entry.getValue().split(SPLIT_KEYWORDS_ON)));
				question.setLanguageToKeywords(langToKeywords);
				question.setLanguageToQuestion(langToQuestion);

				//question.setSparqlQuery(sparql);
				
				DbeQuestions.add(question);
			//	System.out.println(DbeQuestions);

				
			}
			
			return DbeQuestions;
	


}
	public static void function1() throws JsonParseException, JsonMappingException, IOException 
	
	{
		 File initialFile = new File("/home/suganya/lcquad_qaldformat.json");
		    InputStream targetStream = new FileInputStream(initialFile);

		//System.out.println(LoaderController.loadNLQ(targetStream));
		QaldJson json = (QaldJson) ExtendedQALDJSONLoader.readJson(targetStream, QaldJson.class);
		System.out.println(EJQuestionFactory.getQuestionsFromQaldJson(json));
		
	}

}
