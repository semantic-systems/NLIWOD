package org.aksw.mlqa.analyzer.nqs;

import java.util.ArrayList;


public class QueryModuleLibrary {

	/*
	 * returns the word index of "word" in "string" 
	 * or returns -1 if word not found
	 * */
	private static String NOUN_TAG = "NN";
	private static String NOUN_NER_TAG = "NNP-NER";
	private static String NOUN_MODIFIER_TAG = "_NM";
	public static String[] booleanQueriesTokens = {"is","are","did","does","do","has","have"};

	
	public static int wordIndex(String string, String word) {
		String[] words = string.split(" ");
		for(int i=0;i<words.length;i++){
			if(words[i].equals(word))
				return i;
		}
		return -1;
	}
	
	public static ArrayList<QueryToken> mergeTokens(ArrayList<QueryToken> tokens, int start, int end, String tag){	
		try{
			String toMergeString = "";
			for(int i=start;i<end;i++){
				// Used when we need modifier as a different token in QCT. 
				/*if (!Quantifier.isQuantifier(tokens.get(i).getString()) && QueryToken.isNounVariant(tag) && !QueryToken.isNERVariant(tag)) { // Adding modifier
					toMergeString += tokens.get(i).getString()
							+ (i == (end - 1) ? "" : NOUN_MODIFIER_TAG) + " ";
				} else */
					toMergeString += tokens.get(i).getString()+" ";
			}
			
			toMergeString = toMergeString.trim();
			QueryToken REL1Token = new QueryToken(toMergeString,tag);
			tokens.set(start, REL1Token);
			
			for(int i=0;i<end-start-1;i++)
				if(tokens.size()>start+1)
					tokens.remove(start+1);
		}catch(ArrayIndexOutOfBoundsException e){
			System.err.println("ArrayIndexOutOfBoundsException:"+tokens.toString()+" "+start+" "+end + " "+ tag);
		}
		return tokens;
	}

	public static ArrayList<QueryToken> mergeTokens(
		ArrayList<QueryToken> tokens, String queryString,  String stringToMerge, String tag) {
		
		String queryFlagString = queryString.replace(stringToMerge, "%TEMP_Flag%");
		int startIndex = wordIndex(queryFlagString,"%TEMP_Flag%");
		return mergeTokens(tokens,startIndex,startIndex + stringToMerge.split(" ").length,tag);
	}

	public static String getStringFromTokens(ArrayList<QueryToken> tokens) {
		String queryString = "";
		for(QueryToken qt: tokens){
			//Log.d("getString", qt.getString());
			queryString += qt.getString() + " ";
		}
		
		return queryString.trim();
	}
}
