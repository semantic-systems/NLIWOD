package org.aksw.mlqa.analyzer.nqs;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.util.Triple;



public class TokenMerger {
	
	private ArrayList<QueryToken> tokens;
	private String queryString;
	private ner_resolver ner;
	
	public TokenMerger(ArrayList<QueryToken> tokens,String queryString){
		this.tokens = tokens;
		this.queryString = queryString;
		ner = new ner_resolver();
		startMerger();
	}
	
	public TokenMerger(){
		tokens = null;
		ner = new ner_resolver();
	}
	
	public void setTokens(ArrayList<QueryToken> tokens, String queryString){
		this.tokens = tokens;
		this.queryString = queryString;
	}
	
	public void startMerger(){
		
		if(tokens!=null){
			howMerger();
			//Log.d("after How Merger", tokens.toString());

			nerMerger();
			//Log.d("after NER Merger", tokens.toString());

			qauntifierHandler();
			//Log.d("after Quantify Merger", tokens.toString());

			NNmerger();
			//Log.d("after NN Merger", tokens.toString());

		}			
	}

	private void howMerger() {
		if(tokens!=null && tokens.size()>1 && tokens.get(0).getString().equalsIgnoreCase("how")){
			
			/*String nextString = tokens.get(1).getString();
			if(nextString.equalsIgnoreCase("many") || nextString.equalsIgnoreCase("big") ||
					nextString.equalsIgnoreCase("many") || nextString.equalsIgnoreCase("many"))*/
			if(tokens.get(1).getString().equalsIgnoreCase("many") || tokens.get(1).getString().equalsIgnoreCase("much"))
				QueryModuleLibrary.mergeTokens(tokens, 0, 2, "WP");
		}
		
	}

	private void nerMerger() {
		List<Triple<String, Integer, Integer>> nerTags = ner.getNERTags(queryString,true);
		int start;
		String tag;
		if(tokens.size()==nerTags.size()){
			int i=0;
			
			while(i<nerTags.size()){
				if(!nerTags.get(i).first.equals("O")){
					start = i;
					tag = nerTags.get(i).first;
					i++;
					while(start+1<nerTags.size() && nerTags.get(start+1).first.equals(tag)){
						nerTags.remove(start+1);
						i++;
					}
					if(!(start==i-1 && isNumeric(tokens.get(start).getString()))) //Sometime NER classify number as NE. Ignore them.
						QueryModuleLibrary.mergeTokens(tokens, start, i, "NNP-NER");
					
					i = start+1;
				}
				else
					i++;
			}
		}
	}

	private void qauntifierHandler() {
		
		//Log.d("-before", tokens.toString());
		
		for(int i =0;i<tokens.size()-1;i++){
			
			// VBZ   JJ   IN  -> Role
			if(tokens.get(i).isVerbVariant()){
				if(tokens.get(i+1).isAdjVariant())
					if(tokens.size()>i+2 && tokens.get(i+2).isIN())
						QueryModuleLibrary.mergeTokens(tokens, i, i+3, "ROLE");
			} 
			
			// RB  JJ  ->  JJ    ("so beautiful")
			else if(tokens.get(i).isRB()){
				if(tokens.get(i+1).isAdjVariant())
					QueryModuleLibrary.mergeTokens(tokens, i, i+2, "JJ");
			} 
			
			// JJ, JJ -> JJ
			else if(tokens.get(i).isAdjVariant()){
				if(tokens.get(i+1).isAdjVariant())
					QueryModuleLibrary.mergeTokens(tokens, i, i+2, "JJ");
			} 
			
			// VBN, IN -> VBN
			else if(tokens.get(i).isVBN()){
				if(tokens.get(i+1).isIN())
					QueryModuleLibrary.mergeTokens(tokens, i, i+2, "VBN");
			}			
			
			// JJ, NN -> NN
			if(tokens.get(i).isAdjVariant()){
				if(tokens.get(i+1).isNNPNER())
					QueryModuleLibrary.mergeTokens(tokens, i, i+2, "NNP-NER");
				else if(tokens.get(i+1).isNounVariant())
					QueryModuleLibrary.mergeTokens(tokens, i, i+2, "NN");
			} 
			
			// CD -> VBD/VBG/VBN -> NN  (four wheeled car)
			if(tokens.get(i).isCD()){
				if(tokens.get(i+1).isVBDGN()){
					if(tokens.size()>i+2 && tokens.get(i+2).isNNPNER())
						QueryModuleLibrary.mergeTokens(tokens, i, i+3, "NNP-NER");
					else if(tokens.size()>i+2 && tokens.get(i+2).isNounVariant())
						QueryModuleLibrary.mergeTokens(tokens, i, i+3, "NN");
				}
			} 
	
		}
		//Log.d("-s1", tokens.toString());
		
		for(int i =0;i<tokens.size()-1;i++){
			
			if(tokens.get(i).isDT()){
				
				// DT, NN -> NN
				
				if(tokens.get(i+1).isNNPNER())
					QueryModuleLibrary.mergeTokens(tokens, i, i+2, "NNP-NER");
				else if(tokens.get(i+1).isNounVariant())
					QueryModuleLibrary.mergeTokens(tokens, i, i+2, "NN");
				
				// DT, JJ, NN -> NN ( Actually, it is not reachable. We already replaced this in last loop)
				else if(tokens.get(i+1).isAdjVariant()){
					if(i+2<tokens.size()){						
						if(tokens.get(i+2).isNNPNER())
							QueryModuleLibrary.mergeTokens(tokens, i, i+3, "NNP-NER");
						else if(tokens.get(i+2).isNounVariant())
							QueryModuleLibrary.mergeTokens(tokens, i, i+3, "NN");
					}
				}
			}
		}

		//Log.d("-s2", tokens.toString());

		for(int i =0;i<tokens.size()-1;i++){
			
			// RB, *, NN -> NN
			if(!tokens.get(0).getString().toLowerCase().trim().startsWith("how") && tokens.get(i).isRB()){
				for(int j=i+1;j<tokens.size();j++){
					if(tokens.get(j).isNNPNER()){
						QueryModuleLibrary.mergeTokens(tokens, i, j+1, "NNP-NER");
						break;
					}
					else if(tokens.get(j).isNounVariant()){
						QueryModuleLibrary.mergeTokens(tokens, i, j+1, "NN");
						break;
					}
				}
			}
		}		
	}
	
	private void NNmerger() {
		/* ***  Club NN ****/
		// NN, NN(,p,s,ps) -> NN(,p,s,ps)
		// VBG, NN(,p,s,ps) -> NN(,p,s,ps)
		// CD, NN(,p,s,ps) -> NN(,p,s,ps)
		
		for(int i =0;i<tokens.size()-1;i++){
			if(tokens.get(i).isNounVariant() || tokens.get(i).isCD() || tokens.get(i).isVBG()){
				if(tokens.get(i+1).isNounVariant() && !tokens.get(i+1).isNNPNER() ){
					//Log.d("Before NN NN Merger",tokens.toString());
					QueryModuleLibrary.mergeTokens(tokens, i, i+2, tokens.get(i+1).getTag());
					//Log.d("After NN NN Merger",tokens.toString());
				}
			}
		}
	}
	
	public ArrayList<QueryToken> getTokens(){
		return tokens;
	}
	
	public static boolean isNumeric(String str)  
	{  if(str.equalsIgnoreCase("one") || str.equalsIgnoreCase("two")|| str.equalsIgnoreCase("three") || str.equalsIgnoreCase("four")
			|| str.equalsIgnoreCase("five")|| str.equalsIgnoreCase("six")|| str.equalsIgnoreCase("seven")|| str.equalsIgnoreCase("eight")
			|| str.equalsIgnoreCase("nine")|| str.equalsIgnoreCase("ten"))
		return true;
	  try  
	  {  
	    double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}
}
