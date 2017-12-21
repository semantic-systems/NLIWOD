package org.aksw.surfaceformgenerator;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;


public class SurfaceForms {
	public static String DIRECTORY = "data/2016-10/en/";	
	public static String DBPEDIA_MATOLL_FILE = "" + DIRECTORY + "matoll_EN"+".ttl";
	public static String SURFACE_FORMS = "" + DIRECTORY + "_surface_forms.ttl";
	public static void main(String[] args) throws IOException, InterruptedException {
		  
		SURFACE_FORMS = DIRECTORY + "surface_forms.ttl";
	
		   HashMap<String, Set<String>> hmap = new HashMap<>();
		Set tempSet = new HashSet<String>();
		String values = null;
		
		 	FileReader in = new FileReader(DBPEDIA_MATOLL_FILE);
		    BufferedReader br = new BufferedReader(in);
		    while (br.ready()) {
		        String readLine = br.readLine();
		        String[] split =readLine.split("\t");
		        String ar = "<" +split[4] + "> rdfs:label \"" + split[0] + "\" .";
				String str="<http://dbpedia.org/property/";
				if(ar.contains(str)){
				
					String[] property = ar.split(" ");
					String p= property[0]; 
					String l= property[2];
					if(!hmap.containsValue(p)){
						tempSet.add(l);
						hmap.put(p, tempSet);
					}
					else{
						tempSet=hmap.get(l);
						tempSet.add(p);
						hmap.put(l, tempSet);
					}
				}
			}
		    
		    for(HashMap.Entry<String, Set<String>> entry : hmap.entrySet()){
		  
		    values=entry.getKey() +entry.getValue();  
		     System.out.println(values); 
		  
	            
	}

		    
	}
}

