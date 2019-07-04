package org.aksw.mlqa.analyzer.queryanswertype;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.mlqa.analyzer.questiontype.QuestionTypeAnalyzer;
import org.aksw.qa.annotation.index.IndexDBO_properties;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Attribute;

/**
 * Analyzes what type for the result is expected from the DBpedia Ontology.
 * 
 * @author ricardousbeck
 *
 */
public class QueryAnswerTypeAnalyzer implements IAnalyzer {
	private static final  StanfordCoreNLP PIPELINE;
	
	private static final String SERVICE = "http://dbpedia.org/sparql";
	
	private Logger log = LoggerFactory.getLogger(QueryAnswerTypeAnalyzer.class);
	private Attribute attribute = null;
	private IndexDBO_properties index;
	
	static {
		Properties props = new Properties();
	    props.setProperty("annotators","tokenize, ssplit, pos");
	    PIPELINE = new StanfordCoreNLP(props);
	}

	public QueryAnswerTypeAnalyzer() {
		ArrayList<String> attributeValues = new ArrayList<String>();

		attributeValues.add("DBpedia:Activity");
		attributeValues.add("DBpedia:Actor");
		attributeValues.add("DBpedia:AdministrativeRegion");
		attributeValues.add("DBpedia:Agent");
		attributeValues.add("DBpedia:Airline");
		attributeValues.add("DBpedia:Album");
		attributeValues.add("DBpedia:Animal");
		attributeValues.add("DBpedia:Arena");
		attributeValues.add("DBpedia:ArchitecturalStructure");
		attributeValues.add("DBpedia:Artist");
		attributeValues.add("DBpedia:Artwork");
		attributeValues.add("DBpedia:Athlete");
		attributeValues.add("DBpedia:Award");
		attributeValues.add("DBpedia:Band");
		attributeValues.add("DBpedia:BasketballPlayer");
		attributeValues.add("DBpedia:Beverage");
		attributeValues.add("DBpedia:BodyOfWater");
		attributeValues.add("DBpedia:Book");
		attributeValues.add("DBpedia:Bridge");
		attributeValues.add("DBpedia:BritishRoyalty");
		attributeValues.add("DBpedia:Broadcaster");
		attributeValues.add("DBpedia:CelestialBody");
		attributeValues.add("DBpedia:ChristianBishop");
		attributeValues.add("DBpedia:City");
		attributeValues.add("DBpedia:Cleric");
		attributeValues.add("DBpedia:Comedian");
		attributeValues.add("DBpedia:Company");
		attributeValues.add("DBpedia:Continent");
		attributeValues.add("DBpedia:Country");
		attributeValues.add("DBpedia:Currency");
		attributeValues.add("DBpedia:Device");
		attributeValues.add("DBpedia:Disease");
		attributeValues.add("DBpedia:EducationalInstitution");
		attributeValues.add("DBpedia:Eukaryote");
		attributeValues.add("DBpedia:Event");
		attributeValues.add("DBpedia:FictionalCharacter");
		attributeValues.add("DBpedia:Film");
		attributeValues.add("DBpedia:FloweringPlant");
		attributeValues.add("DBpedia:Food");
		attributeValues.add("DBpedia:Game");
		attributeValues.add("DBpedia:GovernmentAgency");
		attributeValues.add("DBpedia:Governor");
		attributeValues.add("DBpedia:Grape");
		attributeValues.add("DBpedia:Holiday");
		attributeValues.add("DBpedia:Infrastructure");
		attributeValues.add("DBpedia:Island");
		attributeValues.add("DBpedia:Language");
		attributeValues.add("DBpedia:Mammal");
		attributeValues.add("DBpedia:MilitaryPerson");
		attributeValues.add("DBpedia:MilitaryUnit");
		attributeValues.add("DBpedia:Model");
		attributeValues.add("DBpedia:Monarch");
		attributeValues.add("DBpedia:Mountain");
		attributeValues.add("DBpedia:MusicalArtist");
		attributeValues.add("DBpedia:MusicalWork");
		attributeValues.add("DBpedia:NaturalPlace");
		attributeValues.add("DBpedia:OfficeHolder");
		attributeValues.add("DBpedia:Organisation");
		attributeValues.add("DBpedia:PersonFunction");
		attributeValues.add("DBpedia:Philosopher");
		attributeValues.add("DBpedia:Planet");
		attributeValues.add("DBpedia:Plant");
		attributeValues.add("DBpedia:PlayboyPlaymate");
		attributeValues.add("DBpedia:Politician");
		attributeValues.add("DBpedia:PopulatedPlace");
		attributeValues.add("DBpedia:ProgrammingLanguage");
		attributeValues.add("DBpedia:RecordLabel");
		attributeValues.add("DBpedia:Region");
		attributeValues.add("DBpedia:River");
		attributeValues.add("DBpedia:Rocket");
		attributeValues.add("DBpedia:RouteOfTransportation");
		attributeValues.add("DBpedia:Royalty");
		attributeValues.add("DBpedia:Saint");
		attributeValues.add("DBpedia:Scientist");
		attributeValues.add("DBpedia:Settlement");
		attributeValues.add("DBpedia:Single");
		attributeValues.add("DBpedia:Software");
		attributeValues.add("DBpedia:SpaceMission");
		attributeValues.add("DBpedia:Species");
		attributeValues.add("DBpedia:Sport");
		attributeValues.add("DBpedia:Stream");
		attributeValues.add("DBpedia:Swimmer");
		attributeValues.add("DBpedia:TelevisionShow");
		attributeValues.add("DBpedia:TelevisionStation");
		attributeValues.add("DBpedia:University");
		attributeValues.add("DBpedia:Weapon");
		attributeValues.add("DBpedia:Website");
		attributeValues.add("DBpedia:Work");
		attributeValues.add("DBpedia:Writer");
		attributeValues.add("DBpedia:WrittenWork");
		attributeValues.add("Schema:AdministrativeArea");
		attributeValues.add("Schema:BodyOfWater");
		attributeValues.add("Schema:Book");
		attributeValues.add("Schema:City");
		attributeValues.add("Schema:CollegeOrUniversity");
		attributeValues.add("Schema:Continent");
		attributeValues.add("Schema:Country");
		attributeValues.add("Schema:CreativeWork");
		attributeValues.add("Schema:EducationalOrganization");
		attributeValues.add("Schema:Event");
		attributeValues.add("Schema:GovernmentOrganization");
		attributeValues.add("Schema:Language");
		attributeValues.add("Schema:Mountain");
		attributeValues.add("Schema:Movie");
		attributeValues.add("Schema:MusicAlbum");
		attributeValues.add("Schema:MusicGroup");
		attributeValues.add("Schema:Organization");
		attributeValues.add("Schema:Person");
		attributeValues.add("Schema:Place");
		attributeValues.add("Schema:Product");
		attributeValues.add("Schema:RiverBodyOfWater");
		attributeValues.add("Schema:TelevisionStation");
		attributeValues.add("Schema:WebPage");
		
		attributeValues.add("DBpedia:Place");
		attributeValues.add("DBpedia:Person");
		attributeValues.add("Schema:Date");
		attributeValues.add("Schema:GYear");
		attributeValues.add("Schema:String");
		attributeValues.add("Number");
		attributeValues.add("Boolean");
		attributeValues.add("Misc");

		attribute = new Attribute("QueryAnswerType", attributeValues);
		index = new IndexDBO_properties();
	}
	
	@Override
	public Object analyze(String q) {
		log.debug("String question: " + q);
		
		//some cases are resolved through the first word of the question
		if(q.startsWith("Where ") || q.startsWith("In ")) return "DBpedia:Place";
		if(q.startsWith("How ")) return "Number";
		if(q.startsWith("When ")) return "Schema:Date";
		if(q.startsWith("Who ")) return "DBpedia:Person";
		if(QuestionTypeAnalyzer.isASKQuestion(q)) return "Boolean";
		
		Annotation annotation = new Annotation(q);
	    PIPELINE.annotate(annotation);
	      
	    List<CoreMap> question = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    
	    //get all nouns, verbs, adjectives
	    List<String> verbs = getWords(question, "V");
	    List<String> nouns = getWords(question, "N");
	    List<String> adjectives = getWords(question, "JJ");
	    
	    //get all properties for the nouns, verbs, adjectives
	    Map<String, List<String>> properties = new LinkedHashMap<>();
	    getProperties(properties, verbs);
	    getProperties(properties, nouns);
	    getProperties(properties, adjectives);
 		
	    //query all ranges for the properties and put them in a list
	    ArrayList<String> ranges = new ArrayList<String>();
 		for(String key: properties.keySet()) {
 			for(String r: properties.get(key)) {
 				String answer = queryRange(r);
 				ranges.add(answer);
 			}
 		}
 		
 		//find the most common range
 		String range = mostCommon(ranges);

 		//set the answertype depending on the uri (xml schema, ontology etc.)
 		if(range.contains("http://dbpedia.org/ontology/")) {
 			return range.replace("http://dbpedia.org/ontology/", "DBpedia:");
 		}  else if(range.contains("http://www.w3.org/2001/XMLSchema#")) {
 			if(range.toLowerCase().contains("double") || range.toLowerCase().contains("integer")) {
 				return "Number";
 			}
 			range = range.replace("http://www.w3.org/2001/XMLSchema#", "");
 			range = range.substring(0,1).toUpperCase() + range.substring(1);
 			return "Schema:" + range;
 		} else if(range.contains("http://www.w3.org/1999/02/22-rdf-syntax-ns#langString")) {
 			return "Schema:String";
 		}
 		return "Misc";
	}
	
	
	/**
	 * Extracts the properties for each word from the through the IndexDBO module of NLIWOD and puts them into the given HashMap.
	 * @param properties afterwards contains the words as keys and a list of their properties as values
	 * @param words
	 */
	private void getProperties(Map<String, List<String>> properties,  List<String> words) {
		for(String word: words) {
			List<String> props = index.search(word);
			if(props.size() > 0) properties.put(word, props);
 		}
	}
	
	/***
	 * Returns all words with the given tag. NN for all nouns, VB for all verbs, JJ for all adjectives.
	 * @param question
	 * @param tag NN for all nouns, VB for all verbs, JJ for all adjectives.
	 * @return list of words with the given tag.
	 */
	private ArrayList<String> getWords(List<CoreMap> question, String tag) {
 		ArrayList<String> words = new ArrayList<String>();
 		
 		for (CoreMap sentence : question) {
            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
            for(CoreLabel token: tokens) {
               	if(token.tag().startsWith(tag)){
            		String word = token.toString();
            		words.add(word.substring(0, word.lastIndexOf("-")));
            	}
            }
        }       	
 		return words;
 	}
	
	/***
	 * Queries the DBpedia SPARQL endpoint for the range of the given property.
	 * @param property
	 * @return range of the property
	 */
	private String queryRange(String property) {	
		String q = "PREFIX rdfs:   <http://www.w3.org/2000/01/rdf-schema#> SELECT ?x {<" + property + "> rdfs:range ?x.  }";	
		QueryExecution qe = QueryExecutionFactory.sparqlService(SERVICE, q);
		ResultSet rs = qe.execSelect();	
		if(rs.hasNext()) {
			QuerySolution solution = rs.nextSolution();			
			RDFNode node = solution.get("?x");	
			return node.toString();
		}	
		return "Misc";
	}	
	
	/**
	 * Returns the most common element from the list.
	 * @param ranges
	 * @return most common element
	 */
	private String mostCommon(ArrayList<String> ranges) {
		HashMap<String, Integer> mC = new HashMap<String,Integer>();
		
		for(String range: ranges) {
			//disregard the Misc range
			if(range.equals("Misc")) continue;
			
			if(mC.containsKey(range)) {
				mC.put(range, mC.get(range) + 1);
			} else {
				mC.put(range, 1);
			}
		}
		
		String maxRange = "";
		int currentMax = 0;
		for(String key: mC.keySet()) {
			if(mC.get(key) > currentMax) {
				maxRange = key;
				currentMax = mC.get(key);
			}
		}
		return maxRange;
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
