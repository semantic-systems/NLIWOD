package org.aksw.mlqa.analyzer.nqs;

import edu.stanford.nlp.ling.TaggedWord;


public class QueryToken extends TaggedWord{

	private String tokenString;
	private String tokenTag;
	private boolean isRole = false, isConcept = false,isRelation = false;
	
	public QueryToken(String string, String tag){
		tokenString = string.trim();
		tokenTag = tag.trim(); 
	}
	
	public String getString(){
		return tokenString;
	}
	
	public String getTag(){
		return tokenTag;
	}
	
	public void setString(String string){
		tokenString = string.trim();
	}
	
	public void setTag(String tag){
		tokenTag = tag.trim();
	}
	
	public boolean tagEquals(String tag){
		return tokenTag.equalsIgnoreCase(tag.trim());
	}
	
	public String toString(){
		return tokenString+"/"+tokenTag;
	}
	
	public String toStringWithoutTags(){
		return tokenString;
	}
	
	
	public boolean isAdjVariant(){
		if(tokenTag.equalsIgnoreCase("JJ") || tokenTag.equalsIgnoreCase("JJR") 
				|| tokenTag.equalsIgnoreCase("JJS"))
			return true;
		else
			return false;
	}
	
	public boolean isNounVariant(){
		if(tokenTag.equalsIgnoreCase("NN") || tokenTag.equalsIgnoreCase("NNS") 
				|| tokenTag.equalsIgnoreCase("NNP") || tokenTag.equalsIgnoreCase("NNPS")
				|| tokenTag.equalsIgnoreCase("NNP-NER"))
			return true;
		else
			return false;
	}
	
	public static boolean isNounVariant(String value){
		if(value.equalsIgnoreCase("NN") || value.equalsIgnoreCase("NNS") 
				|| value.equalsIgnoreCase("NNP") || value.equalsIgnoreCase("NNPS")
				|| value.equalsIgnoreCase("NNP-NER"))
			return true;
		else
			return false;
	}
	
	public static boolean isNERVariant(String value){
		if(value.equalsIgnoreCase("NNP-NER"))
			return true;
		return false;
	}
	
	public boolean isVerbVariant(){
		if(tokenTag.equalsIgnoreCase("VB") || tokenTag.equalsIgnoreCase("VBD") 
				|| tokenTag.equalsIgnoreCase("VBG") || tokenTag.equalsIgnoreCase("VBN")
				|| tokenTag.equalsIgnoreCase("VBP") || tokenTag.equalsIgnoreCase("VBZ"))
			return true;
		else
			return false;
	}
	
/*	public void setRoleToken(){
		isRole = true;
	}
	
	public void setRelationToken(){
		isRelation = true;
	}

	public void setConceptToken(){
		isConcept = true;
	}
	
	public boolean isRole(){
		return isRole;
	}
	
	public boolean isRelation(){
		return isRelation;
	}
	
	public boolean isConcept(){
		return isConcept;
	}*/
	
	public boolean isNN(){
		if(tokenTag.equalsIgnoreCase("NN"))
			return true;
		else
			return false;
	}
	
	public boolean isNNP(){
		if(tokenTag.equalsIgnoreCase("NNP") || tokenTag.equalsIgnoreCase("NNP-NER"))
			return true;
		else
			return false;
	}
	
	public boolean isNNS(){
		if(tokenTag.equalsIgnoreCase("NNS"))
			return true;
		else
			return false;
	}
	
	public boolean isNNPS(){
		if(tokenTag.equalsIgnoreCase("NNPS"))
			return true;
		else
			return false;
	}
	
	public boolean isRB(){
		if(tokenTag.equalsIgnoreCase("RB"))
			return true;
		else
			return false;
	}
	
	public boolean isDT(){
		if(tokenTag.equalsIgnoreCase("DT"))
			return true;
		else
			return false;
	}
	
	public boolean isJJ(){
		if(tokenTag.equalsIgnoreCase("JJ"))
			return true;
		else
			return false;
	}
	
	public boolean isVBN(){
		if(tokenTag.equalsIgnoreCase("VBN"))
			return true;
		else
			return false;
	}
	
	public boolean isVBG(){
		if(tokenTag.equalsIgnoreCase("VBG"))
			return true;
		else
			return false;
	}
	
	public boolean isCD(){
		if(tokenTag.equalsIgnoreCase("CD"))
			return true;
		else
			return false;
	}
	
	public boolean isIN(){
		if(tokenTag.equalsIgnoreCase("IN"))
			return true;
		else
			return false;
	}
	
	public boolean isTO(){
		if(tokenTag.equalsIgnoreCase("TO"))
			return true;
		else
			return false;
	}
	
	public boolean isWP(){
		if(tokenTag.equalsIgnoreCase("WP"))
			return true;
		else
			return false;
	}
	
	public boolean isREL1(){
		if(tokenTag.equalsIgnoreCase("REL1"))
			return true;
		else
			return false;
	}
	
	public boolean isRoleTagged(){
		if(tokenTag.equalsIgnoreCase("ROLE"))
			return true;
		else
			return false;
	}
	
	public boolean isPRP(){
		if(tokenTag.equalsIgnoreCase("PRP") || tokenTag.equalsIgnoreCase("PRP$"))
			return true;
		else
			return false;
	}

	public boolean isClause() {
		if(tokenString.equalsIgnoreCase("who") || tokenString.equalsIgnoreCase("which") ||
				tokenString.equalsIgnoreCase("when") ||tokenString.equalsIgnoreCase("where") ||
				tokenString.equalsIgnoreCase("whose") ||tokenString.equalsIgnoreCase("whom") ||
				tokenString.equalsIgnoreCase("what") || tokenString.equalsIgnoreCase("that"))
			return true;
		else
			return false;
	}

	public boolean isPOS() {
		if(tokenTag.equalsIgnoreCase("POS"))
			return true;
		else
			return false;
	}
	
	public boolean isCC() {
		if(tokenTag.equalsIgnoreCase("CC"))
			return true;
		else
			return false;
	}

	public boolean isVBD() {
		if(tokenTag.equalsIgnoreCase("VBD"))
			return true;
		else
			return false;
	}

	public boolean isVBDGN() {
		if(this.isVBD() || this.isVBG() || this.isVBN())
			return true;
		else
			return false;
	}

	public boolean isNNPNER() {
		if(tokenTag.equalsIgnoreCase("NNP-NER"))
			return true;
		else
			return false;
	}
}