package org.aksw.mlqa.analyzer.nqs;


public class Concept {
	private QueryToken token;
	private int index; //Token index in the sentence
	
	public Concept(QueryToken token, int index) {
		this.token = token;
		this.index = index;
	}
	
	public QueryToken getToken(){
		return token;
	}
	
	public int getIndex(){
		return index;
	}
	
	public void setToken(QueryToken token){
		this.token = token;
	}
	
	public void setIndex(int index){
		this.index =index;
	}
	
	public String getString() {
		return token.getString();
	}
	
	public String toString(){
		return token.toStringWithoutTags();
	}
}
