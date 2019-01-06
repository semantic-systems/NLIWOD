package org.aksw.qa.systems;

public class QANARY extends Gen_HTTP_QA_Sys {

	private static final String URL = "http://qanswer-core1.univ-st-etienne.fr/gerbil";
	
	//possible values: dbpedia, wikidata, dblp, freebase
	private static final String KB = "dbpedia";
	
	public QANARY() {
		super(URL, "qanary", true, true);
		this.getParamMap().put("kb", KB);
	}
	
	public QANARY(String url, String kb) {
		super(url, "qanary", true, true);
		this.getParamMap().put("kb", kb);
	}
}
