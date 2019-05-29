package org.aksw.mlqa.analyzer.querytype;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.aksw.mlqa.analyzer.IAnalyzer;
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
 * Analyzes what type for the result is expected
 * 
 * @author ricardousbeck
 *
 */
public class QueryResourceTypeAnalyzer implements IAnalyzer {
	private static final  StanfordCoreNLP PIPELINE;
		
	private static final String SERVICE = "http://dbpedia.org/sparql";
	
	private Logger log = LoggerFactory.getLogger(QueryResourceTypeAnalyzer.class);
	private Attribute attribute = null;
	private IndexDBO_properties index;
	
	static {
		Properties props = new Properties();
	    props.setProperty("annotators","tokenize, ssplit, pos");
	    PIPELINE = new StanfordCoreNLP(props);
	}
	
	public QueryResourceTypeAnalyzer() {
		ArrayList<String> attributeValues = new ArrayList<String>();

		attributeValues.add("DBpedia:Activity");
		attributeValues.add("DBpedia:Actor");
		attributeValues.add("DBpedia:AdministrativeRegion");
		attributeValues.add("DBpedia:Agent");
		attributeValues.add("DBpedia:Airline");
		attributeValues.add("DBpedia:Album");
		attributeValues.add("DBpedia:Animal");
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
		
		attributeValues.add("Schema:Date");
		attributeValues.add("DBpedia:Place");
		attributeValues.add("DBpedia:Person");
		attributeValues.add("Number");
		attributeValues.add("Misc");

		attribute = new Attribute("QueryResourceType", attributeValues);
		index = new IndexDBO_properties();
	}
	
	@Override
	public Object analyze(String q) {
		log.debug("String question: " + q);
		
		if(q.startsWith("Where ")) return "DBpedia:Place";
		if(q.startsWith("How many") || q.startsWith("How much")) return "Number";
		if(q.startsWith("When ")) return "Schema:Date";
		if(q.startsWith("Who ")) return "DBpedia:Person";
		
		Annotation annotation = new Annotation(q);
	    PIPELINE.annotate(annotation);
	      
	    List<CoreMap> question = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    
	    List<String> verbs = getWords(question, "V");
	    List<String> nouns = getWords(question, "N");
	    List<String> adjectives = getWords(question, "JJ");
	    
	    Map<String, List<String>> properties = new LinkedHashMap<>();
	    getProperties(properties, verbs);
	    getProperties(properties, nouns);
	    getProperties(properties, adjectives);
 		
 		//TODO: use the most common range of all properties
 		for(String key: properties.keySet()) {
 			String answer = queryRange(properties.get(key).get(0));
 			return answer.replace("http://dbpedia.org/ontology/", "DBpedia:");
 		}
 		return "Misc";
	}
	
	private void getProperties(Map<String, List<String>> properties,  List<String> words) {
		for(String word: words) {
			List<String> props = index.search(word);
			if(props.size() > 0) properties.put(word, props);
 		}
	}
	
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

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
	
	public static void main(String[] args) {
		QueryResourceTypeAnalyzer a= new QueryResourceTypeAnalyzer();
		System.out.println(a.analyze("What is the highest mountain in Germany?"));
	}
}
