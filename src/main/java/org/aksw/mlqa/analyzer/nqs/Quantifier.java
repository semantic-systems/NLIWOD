package org.aksw.mlqa.analyzer.nqs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;


public class Quantifier {
	String quantifier;
	String nonQuantifierNoun;
	
	HashSet<String> QuantifierSet;
	static String[] QuantifierPhrases = {
			"some","approximately","about", 
			"close to","just about","roughly", 
			"more or less","around","much","a lot", 
			"a good deal","a great deal","much","very much","a great deal",
			"often","frequently","any", 
			"whatever","whatsoever","the","a",
			"an","no","all","wholly",
			"entirely","completely",
			"totoally","altogether","whole","each","to each one",
			"for each one","from each one","apiece","any","both",
			"another","some other","all","no","none",
			"not any","every","least","most"};
	
	public Quantifier(){
		initialize();
	}
	
	public Quantifier(String input) {
		initialize();
		findQuantifiers(input);
	}
	
	private void initialize(){
		QuantifierSet = new HashSet<String> (Arrays.asList(QuantifierPhrases));
	}
	
	public void findQuantifiers(String input) {
		for(String qp : QuantifierPhrases){
			if(input.toLowerCase().contains(" "+qp+" ") || (input.toLowerCase().startsWith(qp) && input.toLowerCase().contains(qp+" "))
					|| (input.toLowerCase().endsWith(qp) && input.toLowerCase().contains(" "+qp)) || qp.equalsIgnoreCase(input)){
				quantifier = qp;
				nonQuantifierNoun = extractInput(input,qp);
				break;
			} else{
				quantifier = "";
				nonQuantifierNoun = input;
			}
		}
	}
	
	public static boolean isQuantifier(String input){
		for(String s : QuantifierPhrases){
			if(input.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}
	
	private String extractInput(String input, String qp) {  // input = "some cat", qp = "some". result = "cat"
		String result="";
		if(input.toLowerCase().contains(qp.toLowerCase())){
			result = input.replace(/*"(?i)"+*/qp, "");
		}
		return result;
	}

	public String getNonQuantifierNoun(){
		return nonQuantifierNoun;
	}
	
	public String getQuantifier(){
		return quantifier;
	}
}
