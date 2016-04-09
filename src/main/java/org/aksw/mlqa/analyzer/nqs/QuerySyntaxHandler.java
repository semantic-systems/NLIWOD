package org.aksw.mlqa.analyzer.nqs;
import java.util.ArrayList;

public class QuerySyntaxHandler {

	public ArrayList<QueryToken> bringWPinFront(ArrayList<QueryToken> tokens) {
		
		if(tokens==null || tokens.size()==0)
			return tokens;
		
		int i=-1;
		for(i=0;i<tokens.size();i++){
			if(tokens.get(i).isWP())
				break;
		}
		
		if(i==-1 || i==tokens.size())
			return tokens;
		
		if(i==1 && tokens.get(i).getString().equalsIgnoreCase("how")){
			tokens.remove(0);
			return tokens;
		}
		
		if(i==1 && tokens.get(i).getString().equalsIgnoreCase("what")){
			tokens.remove(0);
			return tokens;
		}
		
		for(int j=0; j<i ;j++){
			tokens.add(tokens.remove(0));
		}
		
		//log.debug("syn:", tokens.toString());
		
		for(i=0;i<tokens.size();i++)
			if(tokens.get(i).getString().trim().equals("?"))
				tokens.remove(i);
			
		return tokens;
		
	}

	public ArrayList<QueryToken> handleApostrophe(ArrayList<QueryToken> tokens) {
		if(tokens==null || tokens.size()==0)
			return tokens;
		//log.debug("in Apos", tokens.toString());
		for(int i=1; i<tokens.size() ; i++){
		//String s =getApostrophe(tokens.get(0).getString());
			if(tokens.get(i).isPOS() && tokens.get(i-1).isNounVariant()){
				int POSstart = i-2;
				while(POSstart > 0 && (tokens.get(POSstart).isNounVariant() || tokens.get(POSstart).isCD()
						|| tokens.get(POSstart).isVBDGN())){
					POSstart--;
				}
				POSstart++;
				if(i+1 < tokens.size() && tokens.get(i+1).isCC()){
					while(i+2<tokens.size() && (tokens.get(i+2).isNounVariant()
							|| tokens.get(i+2).isCD() || tokens.get(i+2).isVBDGN())){
						i++;
					}
					if(i+3<tokens.size() && tokens.get(i+2).isPOS()){
						tokens = handlePOS(tokens,POSstart,i+3);
					}
				} else if(i+1 < tokens.size()){
					tokens = handlePOS(tokens,POSstart,i+1);
				}
			}
		}		
		return tokens;
	}

	private ArrayList<QueryToken> handlePOS(ArrayList<QueryToken> tokens,
			int POSstart, int POSend) {
		
		if(POSend>=tokens.size())
			return tokens;
		
		if(!tokens.get(POSend).isNounVariant() && !tokens.get(POSend).isVBDGN())
			return tokens;
		
		int i = POSend;
		POSend++;
		
		while(POSend<tokens.size() && (tokens.get(POSend).isNounVariant() || tokens.get(POSend).isVBDGN()))
			POSend++;
		QueryModuleLibrary.mergeTokens(tokens, i, POSend, "NN");
		
		tokens.add(POSstart, tokens.remove(i));
		tokens.add(POSstart+1, new QueryToken("of","IN"));

		for(int j=POSstart+2;j<i+2;j++){
			if(tokens.get(j).isPOS()){
				tokens.remove(j);
				i--;
			}
		}
		return tokens;
	}

	private String getApostrophe(String s) {
		if(s.endsWith("'s"))
			s = s.substring(0,s.lastIndexOf("'"))+" is";
		else if(s.endsWith("'ll"))
		s = s.substring(0,s.lastIndexOf("'"))+" will";
		else if(s.endsWith("'re"))
			s = s.substring(0,s.lastIndexOf("'"))+" are";
		else if(s.endsWith("'ve"))
			s = s.substring(0,s.lastIndexOf("'"))+" have";
		else if(s.endsWith("'d"))
			s = s.substring(0,s.lastIndexOf("'"))+" did";		
		return s;
	}

}