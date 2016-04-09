package org.aksw.mlqa.analyzer.nqs;


public class QueryNormalizer {
	
	static String[] nonWhQueryTagList = {"Give me a list of","Give me all","Give me","List all","List", "Show me"};
	
	public static String normalizeNonWhQuery(String query){
		for(String s: nonWhQueryTagList){
			if(query.toLowerCase().startsWith(s.toLowerCase())){
				query = query.replace(s, "What is list of");
				break;
			}
		}
		System.out.println("Normalized String:"+query);
		return query;
	}

}
