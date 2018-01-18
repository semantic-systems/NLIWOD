package org.aksw.surfaceformgenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SurfaceFormsMATOLL {
	public static String DIRECTORY = "data/2016-10/en/";
	public static String DBPEDIA_MATOLL_FILE = "" + DIRECTORY + "matoll_EN" + ".ttl";
	public static String SURFACE_FORMS = "" + DIRECTORY + "matoll_EN_surface_forms.ttl";

	public static void main(String[] args) throws IOException, InterruptedException {


		FileReader in = new FileReader(DBPEDIA_MATOLL_FILE);
		BufferedReader br = new BufferedReader(in);
		BufferedWriter bw = new BufferedWriter(new FileWriter(SURFACE_FORMS));
		while (br.ready()) {
			String readLine = br.readLine();
			String[] split = readLine.split("\t");
			String label = split[0];
			String prep = split[1];
			String wordtype = split[2];
			String propertyURI = split[4];
			String namespace = "http://dbpedia.org/ontology/";
			if (wordtype.equals("verb") && propertyURI.startsWith(namespace)) {
				String ar = "<" + propertyURI + "> <http://www.w3.org/2000/01/rdf-schema#label> \"" + label + " " + prep + "\" .";
				System.out.println(ar);
				bw.write(ar);
				bw.newLine();
			}
		}
		br.close();
		bw.close();

	}
}
