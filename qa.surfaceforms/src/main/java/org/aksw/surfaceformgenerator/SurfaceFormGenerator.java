package org.aksw.surfaceformgenerator;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Diego Moussallem
 * @author Ricardo Usbeck <usbeck@informatik.uni-leipzig.de>
 */
public class SurfaceFormGenerator {

	private static Boolean FILTER_SURFACE_FORMS = true;
	public static String DIRECTORY = "data/2016-10/en/";
	public static String LANGUAGE = "en";
//downloaded files
	public static String DBPEDIA_REDIRECTS_FILE = "" + DIRECTORY + "redirects_" + LANGUAGE + ".ttl";
	public static String DBPEDIA_LABELS_FILE = "" + DIRECTORY + "labels_" + LANGUAGE + ".ttl";
	public static String DBPEDIA_DISAMBIGUATIONS_FILE = "" + DIRECTORY + "disambiguations_" + LANGUAGE + ".ttl";
	public static String DBPEDIA_INTER_LANGUAGE_LINKS_FILE = "" + DIRECTORY + "interlanguage_links_" + LANGUAGE + ".ttl";
//generated files
	public static String SURFACE_FORMS_FILE = "" + DIRECTORY + LANGUAGE + "_surface_forms.tsv";
	public static String FILTERED_LABELS_FILE = "" + DIRECTORY + "labels_" + LANGUAGE + "_filtered.ttl";

	public static void main(String[] args) throws IOException, InterruptedException {

//		DBPEDIA_REDIRECTS_FILE = DIRECTORY + "redirects_" + LANGUAGE + ".ttl";
//		DBPEDIA_LABELS_FILE = DIRECTORY + "labels_" + LANGUAGE + ".ttl";
//		DBPEDIA_DISAMBIGUATIONS_FILE = DIRECTORY + "disambiguations_" + LANGUAGE + ".ttl";
//		DBPEDIA_INTER_LANGUAGE_LINKS_FILE = DIRECTORY + "interlanguage_links_" + LANGUAGE + ".ttl";
		SURFACE_FORMS_FILE = DIRECTORY + LANGUAGE + "_surface_forms.tsv";
		FILTERED_LABELS_FILE = DIRECTORY + "labels_" + LANGUAGE + "_filtered.ttl";

		DBpediaSpotlightSurfaceFormGenerator surfaceFormGenerator = new DBpediaSpotlightSurfaceFormGenerator();

//		 if (FILTER_SURFACE_FORMS) {
//		 System.out.println("Starting to filter labels_" + LANGUAGE +".uri!");
//		 Set<String> badUris = new HashSet<String>();
//		 badUris.addAll(NtripleUtil.getSubjectsFromNTriple(DBPEDIA_REDIRECTS_FILE,""));
//		 System.out.println("Finished reading bad redirect uris!");
//		 badUris.addAll(NtripleUtil.getSubjectsFromNTriple(DBPEDIA_DISAMBIGUATIONS_FILE,""));
//		 System.out.println("Finished reading bad disambiguations uris!");
//	
//		 badUris.addAll(NtripleUtil.getSubjectsFromNTriple(DBPEDIA_INTER_LANGUAGE_LINKS_FILE,""));
//		 System.out.println("Finished reading bad interlinks uris!");
//		 
//	//	badUris.addAll(NtripleUtil.getSubjectsFromNTriple(ANCHOR_FILE,""));
//		System.out.println("Finished reading bad anchor uris!");
//		
//		// write the file
//		 BufferedWriter writer = new BufferedWriter(new
//		 FileWriter(FILTERED_LABELS_FILE, false));
//		//
//		 System.out.println("Writing filtered labels file: " +
//		 FILTERED_LABELS_FILE);
//	
//		 Model model = ModelFactory.createDefaultModel();
//		 model.read(new FileInputStream(DBPEDIA_LABELS_FILE), null, "TTL");
//		
//		 StmtIterator statements = model.listStatements();
//		 while (statements.hasNext()) {
//		
//		 Statement statement = statements.next();
//		 String subjectUri = statement.getSubject().getURI();
//		
//		 if (!badUris.contains(subjectUri)) {
//		 // TODO test if this is valid RDF
//		 writer.write(statement.asTriple().toString());
//		 writer.write("\n");
//		 }
//		}
//		//NtripleUtil
//		 writer.close();
//		 // generate the surface forms (and save them to the file) or load
//		// them from a file
//		 surfaceFormGenerator.createSurfaceFormFile();
//		 } else {
//		
//		 // generate the surface forms (and save them to the file) or load
//		// them from a file
		surfaceFormGenerator.createSurfaceFormFile();
//		}

	}

}
