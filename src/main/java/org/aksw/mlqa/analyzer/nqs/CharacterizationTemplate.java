package org.aksw.mlqa.analyzer.nqs;


import java.util.ArrayList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


//import org.apache.commons.collections.map.LinkedMap;


public class CharacterizationTemplate {

	private ArrayList<QueryToken> tokens;
	private ArrayList<Role> roleTokenList;
	private ArrayList<Concept> conceptTokenList;
	private ArrayList<Clause> clauseList;
	private ArrayList<String> inputMap;
 	private String Wh="";  //WHAT, WHERE, WHEN, etc.
	private String R1="";  //Relation 1
	private String D="";   // Desire (or Intent)
	private String R2=""; //Relation 1
	private String I="";   //Input
	private String IQ="";   //Input Quantifier
	private String DQ="";   //Desire Quantifier
	private String characterizedString="";
	private int clauseIndex =0, conceptSuper, conceptSub, roleIndex = 1, index,whIndex;
	private int sIndex = 2; //Structure Index
	//private int conceptCount=0;
	private String NOUN_MODIFIER_TAG = "_NM";
	private boolean isCaracterized = true;
	
	
	public CharacterizationTemplate(ArrayList<QueryToken> tokens) {
		this.tokens = tokens;
		roleTokenList = new ArrayList<Role>();
		conceptTokenList = new ArrayList<Concept>();
		inputMap = new ArrayList<String>();
	}

	public void createTemplate() {
		
		resetTemplate();
		//Log.d("QCT", tokens.toString());
		/*for(QueryToken qt:tokens)
			if(isConcept(qt))
				conceptCount++;
		*/
		fillRoleAndConceptList();
		mergeConsicutiveTokens();
		clauseList = getClauseList();
		//System.out.print("\nConcepts:"+conceptTokenList.size()+" Roles:"+roleTokenList.size()+" Clauses:"+clauseList.size());
		//Log.d("Concepts",conceptTokenList.toString());
		//Log.d("Roles",roleTokenList.toString());
		//System.out.println("hasImplicitDesire:"+hasImplicitDesire()+"   conceptlist size"+conceptTokenList.size());
		//System.out.println("concepts:"+conceptTokenList.toString() +"\nroles:"+roleTokenList.toString()+"\ncluase"+clauseList.toString());
		if((hasImplicitDesire() && conceptTokenList.size() == 1)
				|| ((!hasImplicitDesire()) && conceptTokenList.size() == 2 && roleTokenList.size()<2)) {
			Log.d("QCT", "Simple Fit");
			fitSimpleQuery();
		}
		else if(conceptTokenList.size()>=2){
			Log.d("QCT", "Complex Fit");
			fitComplexQuery();
		} else{
			Log.e("QCT","Neither simple nor complex: "+QueryModuleLibrary.getStringFromTokens(tokens));
			isCaracterized = false;
		}
	}

	

	private void fitSimpleQuery() {
		if(roleTokenList.size()>0)
			R2 = roleTokenList.get(0).getString();
		
		if(conceptTokenList.size()>=2){
			D = conceptTokenList.get(0).getString();
			splitDesireAndQuantifier();
			I = conceptTokenList.get(1).getString();
			splitInputAndQuantifier();
			if(Wh.toLowerCase().startsWith("how"))
				handleHOW();
		}
		else if(hasImplicitDesire()){
			
			I = conceptTokenList.get(0).getString();
			splitInputAndQuantifier();
			D = getImplicitDesire(true);	// Implicit Desire;
			if(roleTokenList.size()>1)
				mergeRolesIntoR2();
			//Log.d("QCT", "has implicit desire: "+ D);
		} else{
			System.err.println("No Concept Found.");
		}
		
		
		D = removeTHE(D);
		I = removeTHE(I);
		inputMap.add("[I="+I+"]");
		
		if(Wh.toLowerCase().trim().startsWith("how")){
			R2 = R1 + " " +R2;
			R1 = "";
		}
		
		/*characterizedString = "[WH] = " + Wh + ", " + "[R1] = " + R1 + ", [" + printQuantifier(DQ,"[DQ]")+" "+ printModifier(D,"[DM]") +"[D] =" + DQ +" " +withoutModifier( D) + 
				"], " + "[R2] = " + R2 + ", ["+ printQuantifier(IQ,"[IQ]")+" "+ printModifier(I,"[IM]")+ "[I] =" + IQ +" " + withoutModifier(I) +"]";*/
		
		characterizedString = "[WH] = " + Wh + ", " + "[R1] = " + R1 + ", [D] = "+DQ+" "+ D+ ", [R2] = " + R2 + ", [I] = "+ IQ + " " + I;	
		
		//SPARQLModule sql = new SPARQLModule();
		//System.err.println(sql.constructSimpleSROquery(D,R2,I));
		//characterizedString = sql.constructSimpleSROquery(D,R2,I);
	}

	
	private String withoutModifier(String value) {
		if(value != null){
			for(String s:value.split(" ")){
				if(s.endsWith(NOUN_MODIFIER_TAG))
					value = value.replace(s, "");
			}
		}
		return value;
	}

	private String printQuantifier(String value, String templateLable) {
		String quantifier = "";
		if(value !=null && !value.isEmpty() && value.trim().length()>0)
			quantifier = templateLable + " = " + value;
		return quantifier;
	}

	private String printModifier(String value, String templateLable) {
		String modifierString = "";
		boolean hasModifier = false;
		if(value!=null){
			for(String s : value.split(" ")){
				if(s.endsWith(NOUN_MODIFIER_TAG)){
					hasModifier = true;
					modifierString += s.substring(0, s.length() - NOUN_MODIFIER_TAG.length())+" ";
				}
			}
		}
		if(hasModifier)
			modifierString = templateLable + " = "+ modifierString;
		return modifierString;
	}

	private void mergeRolesIntoR2() {
		R2 = "";
		for(Role r:roleTokenList){
			R2 += r.getString()+" ";
		}
		R2 = R2.trim();		
	}

	private void handleHOW() {
		if(Wh.contains(" ")){
			String nextWord = Wh.toLowerCase().split(" ")[1];
			if(nextWord.equals("many")){
				D = "count("+D+")";
			} else if(nextWord.equals("much")){
				D = "quantity("+D+")";
			} 
		} else {
			D = "DataProperty("+D+")";
		}
		
	}

	private void fitComplexQuery() { /*conceptTokenList>2*/
		
	
		/*System.out.println();
		 * for(Clause token : clauseList){
			System.out.print(token.getString()+"["+token.getIndex()+"] ");
		}
		System.out.println();
		
		for(Role token : roleTokenList){
			System.out.print(token.getString()+"["+token.getIndex()+"] ");
		}
		System.out.println();

		for(Concept token : conceptTokenList){
			System.out.print(token.getString()+"["+token.getIndex()+"] ");
		}
		
*/
		int conceptIndex;
		/*Implicit Desire*/
		if(hasImplicitDesire()){
			I = conceptTokenList.get(0).getString();
			splitInputAndQuantifier();
			D = getImplicitDesire(false);	// Implicit Desire;
			conceptIndex = 1;
		} else{
			D = conceptTokenList.get(0).getString();
			splitDesireAndQuantifier();
			I = conceptTokenList.get(1).getString();
			conceptIndex = 2;
			if(Wh.toLowerCase().startsWith("how"))
				handleHOW();
		}
		
		//if(roleTokenList.get(conceptIndex))
		if(roleTokenList.size()>0){
			if((clauseList.size()==0 && roleTokenList.get(0).getIndex() < conceptTokenList.get(conceptIndex-1).getIndex() ) 
					|| (clauseList.size()>0 && roleTokenList.get(0).getIndex()<clauseList.get(0).getIndex())){
				R2 = roleTokenList.get(0).getString();
			} else if(roleTokenList.get(0).getIndex() > conceptTokenList.get(conceptIndex-1).getIndex() ){
				//clauseList.add(0, new Clause(roleTokenList.get(0).getToken(),roleTokenList.get(0).getIndex()));
				//roleTokenList.remove(0);
				roleIndex--;
			}
			
			// If R2 is after first Clause.
			else if(clauseList.size()>0 && roleTokenList.get(0).getIndex()>clauseList.get(0).getIndex())
				roleIndex--;
		}
		
		
		D = removeTHE(D);
		I = removeTHE(I);
		inputMap.add("[I="+I+"]");

		if(Wh.toLowerCase().trim().startsWith("how")){
			R2 = R1 + " " +R2;
			R1 = "";
		}
		
		/*characterizedString = "[WH] = " + Wh + ", " + "[R1] = " + R1 + ", ["+ printQuantifier(DQ,"[DQ]")+" "+ printModifier(D,"[DM]") + "[D] =" + DQ +" "+ withoutModifier(D) + "], " 
								+ "[R2] = " + R2 + ", ["+ printQuantifier(IQ,"[IQ]")+" "+ printModifier(I,"[IM]")+"[I1_1] =" + IQ+" "+withoutModifier(I) +"]";*/
		
		characterizedString = "[WH] = " + Wh + ", " + "[R1] = " + R1 + ", [D] = "+DQ+" "+D+", "+ "[R2] = " + R2 + ", [I1_1] = "+IQ + " " +I;
		
	
		
		// CC before first clause
/*		while(conceptTokenList.size()>conceptIndex && 
				(clauseList.size()==0 || conceptTokenList.get(conceptIndex).getIndex() < clauseList.get(0).getIndex()))*/
		while(conceptNext(conceptIndex)){
			String tempInput = removeTHE(conceptTokenList.get(conceptIndex).getString());
			//characterizedString +=" [CC] ["+extractAndPrintQuantifier(tempInput,"[IQ]")+" "+ printModifier(tempInput,"[IM]")+" [I1_"+(conceptIndex+1)+"] = "+withoutModifierAndQuantifier(tempInput)+"]";
			characterizedString +=" [CC] "+" [I1_"+(conceptIndex+1)+"] = "+extractAndPrintQuantifier(tempInput,"[IQ]")+" "+tempInput;
			inputMap.add(",[I1_"+(conceptIndex+1)+"="+ tempInput+"]");

			conceptIndex++;
		}

		
		/*If no CC */
		if(conceptIndex==1)
			conceptSuper = 2;
		else  /*If CC */
			conceptSuper = conceptIndex;
		
		
		
		while(clauseList.size()>clauseIndex || roleTokenList.size()>roleIndex){
			conceptSub=1;
			String role="";
			
			if(roleTokenList.size()>roleIndex)
				role = roleTokenList.get(roleIndex).getString();
			
			if(clausalStructureWithoutClause()){
				roleIndex++;
				characterizedString +=",\n[CL"+(sIndex)+"] = null, "+" [R"+(sIndex+1)+"]= "+role+",";
			} else{
				characterizedString +=",\n[CL"+(sIndex)+"] = "+clauseList.get(clauseIndex).getString()+", [R"+(sIndex+1)+"]= "+role+",";
				if(roleTokenList.size()>roleIndex)
					roleIndex++;
				clauseIndex++;
			}
			
			
			boolean first = true;
			
			while(conceptNext(conceptIndex)){
				
				if(!first)
					characterizedString +=" [CC] ";
				else
					first = false;
				
				String tempInput = removeTHE(conceptTokenList.get(conceptIndex).getString());
				//characterizedString +=" ["+extractAndPrintQuantifier(tempInput,"[IQ]")+" "+printModifier(tempInput,"[IM]")+" [I"+sIndex+"_"+(conceptSub)+"] = "+withoutModifierAndQuantifier(tempInput)+"]";
				characterizedString += " [I"+sIndex+"_"+(conceptSub)+"] = "+extractAndPrintQuantifier(tempInput,"[IQ]")+" "+tempInput;
				inputMap.add("[I"+sIndex+"_"+(conceptSub)+"="+tempInput+"]");

				conceptSub++;
				conceptIndex++;
			}
			
			conceptSuper++;
			sIndex++;
		}
	}
	
	private String withoutModifierAndQuantifier(String value) {
		for(String s:value.split(" ")){
			if(s.endsWith(NOUN_MODIFIER_TAG) || Quantifier.isQuantifier(s)){
				value = value.replace(s, "");
				//System.out.println("Quantifier Or Modifier:"+s);
			}
			//else
				//System.out.println("Not Quantifier or Modifier:"+s);
		}
		return value;
	}

	private String extractAndPrintQuantifier(String value, String templateLable) {
		String quantifierString = "";
		boolean hasQuantifier = false;
		for(String s : value.trim().split(" ")){
			if(Quantifier.isQuantifier(s)){
				hasQuantifier = true;
				quantifierString += s+" ";
			}
		}
		if(hasQuantifier)
			quantifierString = quantifierString;
			//quantifierString = templateLable + " = "+ quantifierString;
		return quantifierString;
	}

	private String removeTHE(String input) {
		if(input==null || input.isEmpty() || input.length()==0)
			return input;
		if(input.trim().startsWith("the "))
			input = input.trim().replace("the ", "");
		return input;
	}

	
	private boolean conceptNext(int conceptIndex) {
		
		boolean clausesEnded = clauseList.size()<=clauseIndex;
		boolean rolesEnded = roleTokenList.size()<=roleIndex;
		
		if(conceptTokenList.size()<=conceptIndex)
			 return false;
		
		if(!clausesEnded && conceptTokenList.get(conceptIndex).getIndex()>clauseList.get(clauseIndex).getIndex())
			return false;
		
		if(!rolesEnded && conceptTokenList.get(conceptIndex).getIndex()>roleTokenList.get(roleIndex).getIndex())
			return false;
		 
		return true;
	}

	private boolean clausalStructureWithoutClause() {
		boolean clausesEnded = clauseList.size()<=clauseIndex;
		boolean rolesEnded = roleTokenList.size()<=roleIndex;
		
		if(clausesEnded)
			return true;
		
		if(rolesEnded && !clausesEnded)
			return false;
		
		if(clauseList.get(clauseIndex).getIndex() <= roleTokenList.get(roleIndex).getIndex())
			return false;
		else
			return true;
		
	}

	private boolean hasImplicitDesire(){
		
		if(clauseList.size()==0){ /*Simple Query Case*/
			return (conceptTokenList.size()==1);
		} 
		else if(conceptTokenList.size()>=2){		/*Complex Query Case*/
			if(conceptTokenList.get(1).getIndex()<clauseList.get(0).getIndex()){
				return false;
			} else{
				return true;
			}
		} else{
			System.err.println("Error in finding Implicit Desire.");
			return false;
		}
		
	}

	private ArrayList<Clause> getClauseList() {
		ArrayList<Clause> clauseList = new ArrayList<Clause>();	
		for(int i=0;i<tokens.size();i++){
			if(i!=whIndex && tokens.get(i).isClause()){
				clauseList.add(new Clause(tokens.get(i),i));
			}
		}		
		
		return clauseList;
	}

	private void splitDesireAndQuantifier() {
		Quantifier qf = new Quantifier(D);
		D = qf.getNonQuantifierNoun();
		DQ = qf.getQuantifier();
		//System.out.println("D:"+D+"   "+"DQ:"+DQ);
	}
	
	private void splitInputAndQuantifier() {
		Quantifier qf = new Quantifier(I);
		I = qf.getNonQuantifierNoun();
		IQ = qf.getQuantifier();
	}

	private String getImplicitDesire(boolean isSimpleFit) {
		
		if(Wh.equalsIgnoreCase("Where")){
			return "location("+I+")";
		} else if(Wh.equalsIgnoreCase("When")){
			return "time("+I+")";
		} else if(Wh.equalsIgnoreCase("What")){
			if(isSimpleFit && (IQ==null || IQ.isEmpty() || IQ.length()==0 || IQ.equalsIgnoreCase("the") || IQ.equalsIgnoreCase("a"))){
				return "definition("+I+")";
			} else{
				return "typeOf("+I+")";
			}
		} else if(Wh.equalsIgnoreCase("who") || Wh.equalsIgnoreCase("whom")){
			return "DataProperty (Person)";
		}
		else if(Wh.toLowerCase().startsWith("how ")){
			String nextString = Wh.split(" ")[1];
			if(nextString.equalsIgnoreCase("many")){
				return "count("+D+")";
			} else if(nextString.equalsIgnoreCase("much")){
				return "quantity("+D+")";
			} else {
				return "DataProperty("+nextString+")";
			}
		}
		
		return null;
	}

	private void resetTemplate() {
		roleTokenList.clear();
		conceptTokenList.clear();
		inputMap.clear();
		Wh="";
		R1="";
		D="";
		R2="";
		I="";
		characterizedString="";
		//conceptCount=0;
	}

	private void fillRoleAndConceptList() {	
		
		// If query starts with "WP"
		if(tokens.get(0).isWP()){
			Wh = tokens.get(0).getString();
			whIndex = 0;
			for(int i=1;i<tokens.size();i++){
				if(isConcept(tokens.get(i)))
					conceptTokenList.add(new Concept(tokens.get(i),i));
				
				// Why Not else-if?? Can a token be both concept and role?
				else if(isRole(tokens.get(i))){
					roleTokenList.add(new Role(tokens.get(i),i));	
					
				}
				
				else if(tokens.get(i).isREL1()){
					R1 = tokens.get(i).getString();
				}
				//else if(!tokens.get(i).getString().equals("?"))
					//System.err.println("Unknown Token Type Found. Cannot be placed in template. Token:"+tokens.get(i).getString()+" " + tokens.get(i).getTag());
			}
		}
		else{  /*When Query doesn't starts with "WP" */
			
			int whIndex=-1;
			
			/*Find WP index*/
			for(int i=0;i<tokens.size();i++){
				if(tokens.get(i).isWP()){
					whIndex = i;
					
					break;
				}
			}
			
			/*If WP index is found*/
			if(whIndex!=-1){
				Wh = tokens.get(whIndex).getString();
				this.whIndex = whIndex;
				int index = whIndex+1;
				
				/* Find first concept after WP's index. 
				 * Add all the roles between first concept and WP's index into role list
				 * */
				while(index<tokens.size() && !isConcept(tokens.get(index))){
					if(isRole(tokens.get(index))){
						roleTokenList.add(new Role(tokens.get(index),index));
					}
					index++;
				}
				/*If concept is found, add it to the concept list. This is out Desire(D)*/
				conceptTokenList.add(new Concept(tokens.get(index),index));

				/*Add the remaining roles and concepts after D into their respective lists*/
				for(int i=index+1;i<tokens.size();i++){
					if(isConcept(tokens.get(i))){
						conceptTokenList.add(new Concept(tokens.get(i),i));
					}
					
					// Why Not else-if?? Can a token be both concept and role?
					if(isRole(tokens.get(i))){
						roleTokenList.add(new Role(tokens.get(i),i));	
					}
					
					if(tokens.get(i).isREL1()){
						R1 = tokens.get(i).getString();
					}
				}
				
				
				// Now go backwards from Wh index
				
				// TODO
			}
			
			else{
				// NEW this.whIndex???
			}
		}
	}
	
	private void mergeConsicutiveTokens() {
		
		//Log.d("Start RoleList:", roleTokenList.toString());
		for(int i=0;i<roleTokenList.size()-1;i++){
			if(roleTokenList.get(i).getIndex()==roleTokenList.get(i+1).getIndex()-1){
				int start = i;
				String text = roleTokenList.get(i).getString()+" "+roleTokenList.get(i+1).getString();
				i++;
				while(i<roleTokenList.size()-1 && roleTokenList.get(i).getIndex()==roleTokenList.get(i+1).getIndex()-1){
					text +=" "+roleTokenList.get(i+1).getString();
					i++;
				}
				roleTokenList.get(start).setToken(new QueryToken(text,"ROLE"));
				for(int j=start+1;j<=i;j++)
						roleTokenList.remove(start+1);
				
				i=start;
			}
		}
		//Log.d("End RoleList:", roleTokenList.toString());

		
		/*for(int i=0;i<conceptTokenList.size()-1;i++){
			if(conceptTokenList.get(i).getIndex()==conceptTokenList.get(i+1).getIndex()-1  
					&& noNERinvolved(conceptTokenList.get(i).getToken(),conceptTokenList.get(i+1).getToken())){
				int start = i;
				String text = conceptTokenList.get(i).getString()+" "+conceptTokenList.get(i+1).getString();
				i++;
				while(i<conceptTokenList.size()-1 && conceptTokenList.get(i).getIndex()==conceptTokenList.get(i+1).getIndex()-1
						&& noNERinvolved(conceptTokenList.get(i).getToken(),conceptTokenList.get(i+1).getToken())){
					text +=" "+conceptTokenList.get(i+1).getString();
					i++;
				}
				conceptTokenList.get(start).setToken(new QueryToken(text,"CONCEPT"));
				for(int j=start+1;j<=i;j++)
					conceptTokenList.remove(start+1);
			}
		}*/
		
	}

	private boolean noNERinvolved(QueryToken token1, QueryToken token2) {
		if(token1.isNNPNER() || token2.isNNPNER())
			return false;
		else
			return true;
	}

	public static boolean isConcept(QueryToken token) {
		if(token.isNounVariant() || token.isRB() || token.isCD() || token.isVBG() || token.isAdjVariant())
			return true;
		else
			return false;
	}
	
	public static boolean isRole(QueryToken token) {
		if(token.isRoleTagged() || token.isTO() || token.isIN() || token.isPRP() ||(token.isVerbVariant() && !token.isVBG()))
			return true;
		else
			return false;
	}
	
	private String toStringFromTokenList(ArrayList<QueryToken> tokenList) {
		String result = "";
		for(QueryToken token: tokenList){
			result +=token.getString()+" ";
		}
		return result.trim();
	}
	
	public String getCharacterizedString(){
		if(isBooleanQuery()){
			return "[Concepts] = "+conceptTokenList.toString() +", [Roles] = "+roleTokenList.toString();
		}
		return characterizedString;
	}

	private boolean isBooleanQuery() {
		for(String string : QueryModuleLibrary.booleanQueriesTokens){
			if(Wh.toLowerCase().equals(string))
				return true;
		}
		return false;
	}

	public String getDesire() {
		return D;
	}
	
	public String getInputs(){
		if(inputMap!=null)
			return inputMap.toString();
		else
			return null;
	}

	public String getWH() {
		if(Wh!=null)
			return Wh;
		return null;
	}

	public boolean isCaracterized() {
		
		return isCaracterized;
	}
	
	public ArrayList<Concept> getConceptTokenList(){
		return conceptTokenList;
	}
	
	public ArrayList<Role> getRoleTokenList(){
		return roleTokenList;
	}
}
