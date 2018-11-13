package org.aksw.surfaceformgenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDFS;

public class PropertySurfaceForms {
	public static String DIRECTORY = "data/2016-10/en/";
	public static String LANGUAGE = "en";

	// downloaded files

	public static String DBPEDIA_PROPERTY_FILE = "" + DIRECTORY + "infobox_property_definitions_"+LANGUAGE +".ttl";
	

	// generated files

	public static String PROPERTY_SURFACE_FORMS = "" + DIRECTORY + "_surface_forms.ttl";

	public static void main(String[] args) throws IOException, InterruptedException {

		PROPERTY_SURFACE_FORMS = DIRECTORY + "property_surface_forms.ttl";

		// create an empty model
		Model inputModel = ModelFactory.createDefaultModel();

		// use the FileManager to find the input file
		InputStream in = FileManager.get().open(DBPEDIA_PROPERTY_FILE);
		if (in == null) {
			throw new IllegalArgumentException("File: " + DBPEDIA_PROPERTY_FILE + " not found");
		}

		// read the RDF/XML file
		inputModel.read(in, null, "N-TRIPLE");
		inputModel.write(System.out);

		Model outputModel = ModelFactory.createDefaultModel();
		QueryExecution qExec = QueryExecutionFactory
				.create("SELECT ?s  ?label WHERE{?s a <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property>."
						+ " ?s <http://www.w3.org/2000/01/rdf-schema#label> ?label. }", inputModel);
		ResultSet rs = qExec.execSelect();
		System.out.println(qExec);
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			Resource uri = qs.get("?s").asResource();
			RDFNode label = qs.get("?label");
			StatementImpl s = new StatementImpl(uri, RDFS.label, label);
			outputModel.add(s);

		}

		qExec.close();

		FileOutputStream outputFile = new FileOutputStream(PROPERTY_SURFACE_FORMS);
		RDFDataMgr.write(outputFile, outputModel, RDFFormat.NTRIPLES);
	}
}
