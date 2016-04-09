package org.aksw.mlqa.analyzer.nqs;

import java.util.ArrayList;
import java.io.*;
import jxl.write.*;
import lombok.extern.slf4j.Slf4j;


@Slf4j public class Tester {
	private static QueryBuilder qb;
	static int correctDesires = 0, correctInputs = 0, bothCorrect = 0;;
	static int incorrectDesires = 0, incorrectInputs, notcharacterized=0;
	static WritableWorkbook workbooktowrite;
	static WritableSheet incorrectSheet;
	static ArrayList<Integer> twoSets;
	static ArrayList<Integer> threeSets;
	static int how=0,what=0,when=0,where=0,which=0,who=0,nonwh=0;
	static int ehow=0,ewhat=0,ewhen=0,ewhere=0,ewhich=0,ewho=0,enonwh=0;
	static ArrayList<String> nonwhString;

	public static void main(String[] args) throws IOException, WriteException {
		qb = new QueryBuilder();
		nonwhString = new ArrayList<>();
		qb.setQuery("Who was the first president of independent India?");
		//qb.setQuery("What are some fresh water lakes in lower Himalayas?");
		qb.buildQuery();
		//System.out.println("\nQuery "+i+":"+query);//+"\n"+qb.getTaggedString());
		System.out.println(qb.getCharacterizedString());
		log.debug("QCT",qb.getCharacterizedString());
		log.debug("TAGGED",qb.getTaggedString());
			//queryTest();
		

		log.debug("How", how);
		log.debug("What", what);
		log.debug("Where", where);
		log.debug("Which", which);
		log.debug("When", when);
		log.debug("Who", who);
		log.debug("nonWh", nonwh);
		log.debug("eHow", ehow);
		log.debug("eWhat", ewhat);
		log.debug("eWhere", ewhere);
		log.debug("eWhich", ewhich);
		log.debug("eWhen", ewhen);
		log.debug("eWho", ewho);
		log.debug("nonWhString", nonwhString.toString());
		log.debug("notcharacterized", notcharacterized);
		log.debug("Correct Desire", correctDesires);
		log.debug("Incorrect Desire", incorrectDesires);		
		log.debug("Correct Input", correctInputs);
		log.debug("Incorrect Input", incorrectInputs);
		log.debug("Both correct", bothCorrect);
	}



	private static void queryTest() {
		//QueryTokenizer qt = new QueryTokenizer("What is the subcategories of science?");
		//qt.createAndGetTokenList("I-I I I am Sam?");


		//System.out.println(qt.getTaggedString());

		String[] queries = {
				"When did Jane Doe visit The United States Of America?",
				"When did Jane Doe visit Buffalo located in New York state?",
				"What is the name of the highest mountain which is located in Himalayas.",
				"Which is the smallest lake in lower Himalayas?",
				"Who is the most successful captain of Indian Cricket Team?",
				"What are some salt lakes in lower Himalayas?",
				"Who was the prime minister of Unite States of America in 1992?",
				"Where is the state capital of Missouri which is so beautiful?",
				"What are some dangerous animals and plants which live under the sea?",
				"What is most populous democracy in the Caribbean which is geographically the largest?",
				"What is the distance between Buffalo and New York?",
				"Who was the captain of India, England and Pakistan in cricket world cup which was played in 2011?",
				"What are some wild animals which live in arctic ocean?",
		"Which was the most popular song in Bollywood in 2003 and 2014?"};

		
		//ner_resolver ner = new ner_resolver();
		//List<Triple<String, Integer, Integer>> nerTags;
		int i=1;
		for(String query : queries){
			qb.setQuery(query);
			qb.buildQuery();
			System.out.println("\nQuery "+i+":"+query);//+"\n"+qb.getTaggedString());
			System.out.println(qb.getCharacterizedString());
			//nerTags = ner.getNERTags(query,true);
			//System.out.println(ner.)
			i++;
		}
		//nerTags = ner.getNERTags("What is Statue of Liberty?",false);
		/*POStag tagger = new POStag();
				System.out.println(tagger.getTaggedSentence("In which country DA-IICT is located?"));*/
	}

}