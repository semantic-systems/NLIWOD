package org.aksw.qa.annotation.comparison;

import java.util.ArrayList;

public enum ComparisonEnum {
	LONG("http://dbpedia.org/ontology/length" ),
	LONGER("http://dbpedia.org/ontology/length", "DESC"),
	LONGEST("http://dbpedia.org/ontology/length", "DESC"),
	OLD("http://dbpedia.org/ontology/openingYear,http://dbpedia.org/ontology/birthDate"),
	OLDER("http://dbpedia.org/ontology/openingYear,http://dbpedia.org/ontology/birthDate", "DESC"),
	OLDEST("http://dbpedia.org/ontology/openingYear,http://dbpedia.org/ontology/birthDate", "DESC"),
	TALL("http://dbpedia.org/ontology/height"),
	TALLER("http://dbpedia.org/ontology/height","DESC"),
	TALLEST("http://dbpedia.org/ontology/height", "DESC"),
	SHORT("http://dbpedia.org/ontology/height"),
	SHORTER("http://dbpedia.org/ontology/height","ASC"),
	SHORTEST("http://dbpedia.org/ontology/height" , "ASC"),
	HIGH("http://dbpedia.org/ontology/elevation"),
	HIGHER("http://dbpedia.org/ontology/elevation,http://dbpedia.org/property/higher","DESC"),
	HIGHEST("http://dbpedia.org/ontology/elevation,http://dbpedia.org/property/highest" , "DESC"),
	SMALL("http://dbpedia.org/ontology/areaTotal"),
	SMALLER("http://dbpedia.org/ontology/areaTotal","ASC"),
	SMALLEST("http://dbpedia.org/ontology/areaTotal" , "ASC"),
	LARGE ("http://dbpedia.org/ontology/areaTotal"),
	LARGER("http://dbpedia.org/ontology/areaTotal","DESC"),
	LARGEST("http://dbpedia.org/ontology/areaTotal", "DESC"),
	BIG("http://dbpedia.org/ontology/areaTotal"),
	BIGGER("http://dbpedia.org/ontology/areaTotal","DESC"),
	BIGGEST("http://dbpedia.org/ontology/areaTotal","DESC");
 
	private String order;
	
	private ArrayList<String> uris = new ArrayList<String>();
	  
	private ComparisonEnum(String pURI){
		String[] uris = pURI.split(",");
		for(String u: uris) {
			this.uris.add(u);
		} 	    
	}
	
	private ComparisonEnum(String pURI, String pOrder) {
		this.order = pOrder;
	    String[] uris = pURI.split(",");
	    for(String u: uris) {
	    	this.uris.add(u);
	    }   
	}
	
	public ArrayList<String> getURIS() {
		return this.uris;
	}

	public String getOrder() {
		return this.order;
	}
}
