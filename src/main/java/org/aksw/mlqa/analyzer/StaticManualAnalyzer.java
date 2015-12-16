package org.aksw.mlqa.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;

public class StaticManualAnalyzer {

	private Attribute fmeasureAtt = new Attribute("fmeasure");
	public FastVector fvWekaAttributes = new FastVector();
	private HashMap<String, Instance> map = new HashMap<String, Instance>();

	/**
	 *
	 * @param ClassAttribute
	 *            classes to be differentiated FastVector fvClassVal = new
	 *            FastVector(2); fvClassVal.addElement("positive");
	 *            fvClassVal.addElement("negative");
	 */
	public StaticManualAnalyzer() {
		// declare attributes
		// Question Type: List, Resource, Boolean, Number

		FastVector fvClassValQuestionType = new FastVector(4);
		fvClassValQuestionType.addElement("List");
		fvClassValQuestionType.addElement("Resource");
		fvClassValQuestionType.addElement("Boolean");
		fvClassValQuestionType.addElement("Number");
		fvWekaAttributes.addElement(new Attribute("QuestionType", fvClassValQuestionType));

		// Answer Resource Type: Person, Organization, Location, Misc, Boolean,
		// Number, Date
		FastVector fvClassValAnswerResourceType = new FastVector(7);
		fvClassValAnswerResourceType.addElement("Misc");
		fvClassValAnswerResourceType.addElement("Date");
		fvClassValAnswerResourceType.addElement("Boolean");
		fvClassValAnswerResourceType.addElement("Number");
		fvClassValAnswerResourceType.addElement("Person");
		fvClassValAnswerResourceType.addElement("Organization");
		fvClassValAnswerResourceType.addElement("Location");
		fvWekaAttributes.addElement(new Attribute("AnswerResourceType", fvClassValAnswerResourceType));

		// Wh-type: Command, Who, Which, Ask, How, In which, What, When, Where
		FastVector fvClassValWhtype = new FastVector(9);
		fvClassValWhtype.addElement("Command");
		fvClassValWhtype.addElement("Who");
		fvClassValWhtype.addElement("Which");
		fvClassValWhtype.addElement("Ask");
		fvClassValWhtype.addElement("How");
		fvClassValWhtype.addElement("In which");
		fvClassValWhtype.addElement("What");
		fvClassValWhtype.addElement("When");
		fvClassValWhtype.addElement("Where");
		fvWekaAttributes.addElement(new Attribute("WhType", fvClassValWhtype));

		// #Token numeric
		Attribute token = new Attribute("token");
		fvWekaAttributes.addElement(token);

		// Limit (includes order by and offset): Boolean
		FastVector fvClassValLimit = new FastVector(2);
		fvClassValLimit.addElement("TRUE");
		fvClassValLimit.addElement("FALSE");
		fvWekaAttributes.addElement(new Attribute("Limit", fvClassValLimit));

		// Comparative : Boolean
		FastVector fvClassValComparative = new FastVector(2);
		fvClassValComparative.addElement("TRUE");
		fvClassValComparative.addElement("FALSE");
		fvWekaAttributes.addElement(new Attribute("Comparative", fvClassValComparative));

		// Superlative : Boolean
		FastVector fvClassValSuperlative = new FastVector(2);
		fvClassValSuperlative.addElement("TRUE");
		fvClassValSuperlative.addElement("FALSE");
		fvWekaAttributes.addElement(new Attribute("Superlative", fvClassValSuperlative));

		// Person : Boolean
		FastVector fvClassValPerson = new FastVector(2);
		fvClassValPerson.addElement("TRUE");
		fvClassValPerson.addElement("FALSE");
		fvWekaAttributes.addElement(new Attribute("Person", fvClassValPerson));

		// Location: Boolean
		FastVector fvClassValLocation = new FastVector(2);
		fvClassValLocation.addElement("TRUE");
		fvClassValLocation.addElement("FALSE");
		fvWekaAttributes.addElement(new Attribute("Location", fvClassValLocation));

		// Organization: Boolean
		FastVector fvClassValOrganization = new FastVector(2);
		fvClassValOrganization.addElement("TRUE");
		fvClassValOrganization.addElement("FALSE");
		fvWekaAttributes.addElement(new Attribute("Organization", fvClassValOrganization));

		// Misc: Boolean
		FastVector fvClassValMisc = new FastVector(2);
		fvClassValMisc.addElement("TRUE");
		fvClassValMisc.addElement("FALSE");
		fvWekaAttributes.addElement(new Attribute("Misc", fvClassValMisc));

		// put the fmeasure/class attribute
		fvWekaAttributes.addElement(fmeasureAtt);

		// load file
		try {
	        BufferedReader br = new BufferedReader(new FileReader(new File("manual_features.tsv")));
	        while(br.ready()){
	        	String line = br.readLine();
	        	String split[] = line.split("\t");
				Instance tmpInstance = new Instance(fvWekaAttributes.size());
				tmpInstance.setValue(fvClassValQuestionType,split[2]);
	        	
	        }
        } catch (IOException e) {
	        e.printStackTrace();
        }
		

	}

	/**
	 *
	 * @param q
	 * @return feature vector leaving out a slot for the class variable, i.e.,
	 *         the QA system that can answer this feature vector
	 */
	public Instance analyze(String q) {

		return map.get(q);

	}

	public Attribute getClassAttribute() {
		return fmeasureAtt;
	}

}