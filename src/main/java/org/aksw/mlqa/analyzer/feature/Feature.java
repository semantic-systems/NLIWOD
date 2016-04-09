package org.aksw.mlqa.analyzer.feature;

import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class Feature {
//Question Type    Answer Resource Type    Wh-type    #Token    $Limit (includes order by and offset)$   
//	Comparative    Superlative    Person    Location    Organization    Misc"
	
	static String feature(Nqs n){
		String ques = n.nlQuery;
		String nqs = n.qct;
		String ner = n.ner;
		return questionType(n) +"\t"+ answerType (n)+"\t"+ whType (n)+"\t"+ tokenCount(n)+"\t"+ QuestionType () +"\t"+ isComparative()+"\t"
				+ isSuperlative(n)+"\t"+ isPerson(n) +"\t"+ isLocation(n) +"\t"+ isLocation( n)+"\t" + isOrganization( n)+"\t" +isMisc( n);
	}
	
	static String questionType (Nqs n){
		//Boolean List Number Resource
		if (n.qct.contains("] =  list")){
			return "LIST";	
		}
		else if (n.qct.contains("[Concepts] = [")){
			return "BOOLEAN";
		}
		else if (n.nlQuery.toLowerCase().contains("how many")){
			return "NUMBER";	
		}
		else if (n.getDesire().contains("DataProperty")){
			return "RESOURCE";
		}
		else
			return "RESOURCE";
	}
	
	static String answerType (Nqs n){
		//Boolean Person Misc Organization Number
		return null;
	}
	
	static String whType (Nqs n){
		//Ask Command Which what How Where
		if (n.qct.contains("[Concepts] = ["))
			return "ASK";
		if (n.getDesire().contains("list"))
			return "COMMAND";
		else 
			return n.getWh();	
	}
	
	static int tokenCount (Nqs n){
		//Boolean List Number Resource
		return StringUtils.countMatches(n.qct, "=");
	}
	
	static String QuestionType (){
		//Boolean List Number Resource
		return null;
	}
	
	static boolean isComparative(){
		return true;
	} 
	
	static boolean isSuperlative(Nqs n){
		Pattern superlativeWordList = Pattern.compile("highest|lowest|deepest|fastest|longest|largest|youngest|oldest|heaviest|lightest|tallest|shortest");
		if (superlativeWordList.matcher(n.nlQuery).find())
			return true;
		return false;
	}
	
	static boolean isPerson(Nqs n){
		if(n.ner.contains("PERSON"))
		return true;
		else
		return false;
	}
	
	static boolean isLocation(Nqs n){
		if(n.ner.contains("LOCATION"))
			return true;
			else
			return false;
	}
	
	static boolean isOrganization(Nqs n){
		if(n.ner.contains("ORGANIZATION"))
			return true;
			else
			return false;
	}
	
	static boolean isMisc(Nqs n){
		if(n.ner.contains("MISC"))
			return true;
			else
			return false;
	}
}
