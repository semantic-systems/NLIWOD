package org.aksw.mlqa.analyzer.nqs;

import java.util.ArrayList;

/*
 * Returns tokens of Query Sentence.
 * Each token has a word(s) and the tag associated with it. 
 * */

public class QueryTokenizer {
	private ArrayList<QueryToken> tokenList;
	PosTag tagger;
	//private String[] booleanQueriesTokens = {"is","are","did","does","do","has","have"};
	
	public QueryTokenizer(String QuestionString){
		tokenList = new ArrayList<QueryToken>();
		tagger = new PosTag();
		createTokenList(QuestionString);
	}
	
	public QueryTokenizer(){
		tokenList = new ArrayList<QueryToken>();
		tagger = new PosTag();
	}
	
	/*
	 * creates token list using POStagger
	 * input: Query Sentence
	 * */
	public void createTokenList(String QuestionString){
		tokenList.clear();
		String taggedSentence = tagger.getTaggedSentence(QuestionString);
		System.out.println("Tagged:"+taggedSentence);
		for(String t : taggedSentence.split(" ")){
			if(t.split("_").length==2)
				tokenList.add(new QueryToken(t.split("_")[0],t.split("_")[1]));
			else
				Log.e("TAG Splitting Error", taggedSentence);
		}
		BooleanHandeler(QuestionString);
		replaceWDT();
	}
	
	/* Set Is/Are type questions as WH-Queries
	 * 
	 * */
	private void BooleanHandeler(String questionString) {
		for(String string : QueryModuleLibrary.booleanQueriesTokens){
			if(questionString.toLowerCase().startsWith(string+" ")){
				tokenList.get(0).setTag("WP");
				break;
			}
		}
		
	}

	/*
	 * replaces WDT with WP
	 * */
	private void replaceWDT() {
		for(int i=0;i<tokenList.size();i++){
			if(tokenList.get(i).tagEquals("WDT") || tokenList.get(i).tagEquals("WRB"))
				tokenList.set(i, new QueryToken(tokenList.get(i).getString(),"WP"));
		}
	}

	public ArrayList<QueryToken> getTokenList(){
		return tokenList;
	}
	
	public ArrayList<QueryToken> createAndGetTokenList(String QuestionString){
		createTokenList(QuestionString);
		return tokenList;
	}
	
	public String getTaggedString(){
		return tokenList.toString();
	}
	
	public void reset(){
		tokenList.clear();
	}
}
