package org.aksw.mlqa.analyzer.nqs;

import java.util.ArrayList;


public class AuxRelations {

	private String queryString;
	private ArrayList<QueryToken> tokens;
	
	static String[] aux_words = { "is of several type of",
		"is any of several types of", "is the most important kind of",
		"is a most important kind of", "is an most important kind of",
		"is the some important kind of", "is a some important kind of",
		"is any member of", "is an important kind of",
		"is a taxonomic category of", "is a broad category of",
		"members of", "members of", "is members of", "is members of",
		"is the most important kind of", "is an important kind of",
		"are the sub-categories of","are the subcategories of",
		"is a sub-category of", "is a subset of", "is a sub-class of",
		"is a subcategory of", "is a category of",
		"is a particular kind of", "is a generic type of",
		"is any member of", "are a kind of", "any of several types of",
		"is a part of the order", "is very large type of",
		"are members of class", "is member of class",
		"are in the subclass", "are a type of", "is a special type of",
		"is a type of", "is in the subclass", "is kind of", "kind of",
		"is type of", "is subclass of", "is subset of", "are kind of",
		"are type of", "are subclass of", "are subset of",
		"are members of class", "are member of class",
		"is member of class", "is members of class", "is", "are", "being",
		"'s", "'m", "'re", "'ve", "'s", "'d", "'ll", "has been",
		"have been", "had been", "will be", "shall be", "can be",
		"could be", "would be", "should be", "may be", "might be",
		"must be", "has been", "have been", "had been", "being", "is",
		"are", "was", "were", "does", "do", "did", "will", "shall", "can",
		"could", "would", "should", "may", "might", "must" };
	
	public AuxRelations(String input, ArrayList<QueryToken> tokens){
		this.queryString = input;
		this.tokens = tokens;
		handleAuxRelations();
	}
	
	public ArrayList<QueryToken> getTokens(){
		return tokens;
	}
	
	/*
	 * Replaces the Aux relation into a single Query Token.
	 * Example: initial tokens: 	 "[What/WP, is/VBZ, the/DT, subcategories/NNS, of/IN, science/NN]"
	 * 			After Aux Handeling: "[What/WP, are the subcategories of/REL1, science/NN]"
	 * */
	private void handleAuxRelations() {
		String auxString = "";
		String queryFlagString = "";
		//Log.d("AUX Handeler", queryString);
		for(String str: aux_words){
			if(queryString.contains(" "+str+" ")){
				auxString =str;
				break;
			}
		}		
		
		if(auxString.length()>0){
			//Log.d("Aux", auxString);
			tokens = QueryModuleLibrary.mergeTokens(tokens, queryString,auxString, "REL1");
		}
	}


}
