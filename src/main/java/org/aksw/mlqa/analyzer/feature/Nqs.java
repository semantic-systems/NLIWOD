package org.aksw.mlqa.analyzer.feature;

import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@ToString
@Slf4j public class Nqs {

	public final String nlQuery;
	public final String qct;
	public final String queryid;
	public final String ner;

	//-----------------------------------------------------------------------//
	public String getConcepts(){
		String temp = qct;

		temp = temp.substring(qct.indexOf("[Concepts] = [")+14);//, qct.indexOf("]"));

		temp= temp.substring(0, temp.indexOf("]"));
		log.debug(temp);

		return temp;

	}
	public String getRoles(){
		String temp = qct;
		temp = temp.substring(qct.indexOf("[Roles] = [")+11);//, qct.indexOf("]"));
		temp= temp.substring(0, temp.indexOf("]"));
		log.debug(temp);
		return temp;

	}
	//-----------------------------------------------------------------------//
	public String getWh(){
		String temp;
		temp = qct.substring(qct.indexOf("[WH] =")+6);
		temp = temp.substring(0, temp.indexOf(","));
		return temp.trim();
	} 
	public String getDesire(){
		String temp;
		temp = qct.substring(qct.indexOf("[D] =")+5);
		temp = temp.substring(0, temp.indexOf(","));
		return temp.trim();
	}
	
	public String getRelation2(){
		String temp;
		temp = qct.substring(qct.indexOf("[R2] =")+6);
		temp = temp.substring(0, temp.indexOf(","));
		return temp.trim();
	}
	public String getRelation1(){
		String temp;
		temp = qct.substring(qct.indexOf("[R1] =")+6);
		temp = temp.substring(0, temp.indexOf(","));
		return temp.trim();
	}
	public String getInput(){
		String temp;
		temp = qct.substring(qct.indexOf("[I] =")+5);
		temp = temp.substring(0, (temp+",").indexOf(","));
		return temp.trim();
	}

	public String getAll(){
		return nlQuery + "\n" + qct;
	}

	public String getAllInputs(){
		String tempinput="";
		if(qct.contains("[I]"))
			return getInput();
		else
		{
			tempinput = qct.substring(qct.indexOf("[I1_1]"),qct.indexOf ("["));
			tempinput =tempinput +", "+ qct.substring(qct.indexOf("[I1_2]"),qct.indexOf ("["));
			tempinput =tempinput +", "+ qct.substring(qct.indexOf("[I1_3]"),qct.indexOf ("["));
			return tempinput;
		}
	}
	public String getDesireBrackets()
	{
		//[D] = count(languages)
		String s1;
		s1 = getDesire();
		int i , j;
		i = s1.lastIndexOf("(")+1;
		j = s1.indexOf(')', i);
		return s1.substring(i, j);
	}

}