package org.aksw.mlqa.analyzer.querytype;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.apache.jena.rdf.model.Resource;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;
import weka.core.FastVector;

//TODO write unit test for this analyzer
/**
 * Analyzes what type for the result is expected
 * 
 * @author ricardousbeck
 *
 */
public class QueryResourceTypeAnalyzer implements IAnalyzer {
	private Logger log = LoggerFactory.getLogger(QueryResourceTypeAnalyzer.class);
	private Attribute attribute = null;
	private ASpotter spotter;

	public QueryResourceTypeAnalyzer() {
		FastVector attributeValues = new FastVector();
		// attributeValues.addElement("Place");
		// attributeValues.addElement("Person");
		// attributeValues.addElement("Organization");
		// attributeValues.addElement("Misc");

		attributeValues.addElement("DBpedia:Activity");
		attributeValues.addElement("DBpedia:Actor");
		attributeValues.addElement("DBpedia:AdministrativeRegion");
		attributeValues.addElement("DBpedia:Agent");
		attributeValues.addElement("DBpedia:Airline");
		attributeValues.addElement("DBpedia:Album");
		attributeValues.addElement("DBpedia:Animal");
		attributeValues.addElement("DBpedia:ArchitecturalStructure");
		attributeValues.addElement("DBpedia:Artist");
		attributeValues.addElement("DBpedia:Artwork");
		attributeValues.addElement("DBpedia:Athlete");
		attributeValues.addElement("DBpedia:Award");
		attributeValues.addElement("DBpedia:Band");
		attributeValues.addElement("DBpedia:BasketballPlayer");
		attributeValues.addElement("DBpedia:Beverage");
		attributeValues.addElement("DBpedia:BodyOfWater");
		attributeValues.addElement("DBpedia:Book");
		attributeValues.addElement("DBpedia:Bridge");
		attributeValues.addElement("DBpedia:BritishRoyalty");
		attributeValues.addElement("DBpedia:Broadcaster");
		attributeValues.addElement("DBpedia:CelestialBody");
		attributeValues.addElement("DBpedia:ChristianBishop");
		attributeValues.addElement("DBpedia:City");
		attributeValues.addElement("DBpedia:Cleric");
		attributeValues.addElement("DBpedia:Comedian");
		attributeValues.addElement("DBpedia:Company");
		attributeValues.addElement("DBpedia:Continent");
		attributeValues.addElement("DBpedia:Country");
		attributeValues.addElement("DBpedia:Currency");
		attributeValues.addElement("DBpedia:Device");
		attributeValues.addElement("DBpedia:Disease");
		attributeValues.addElement("DBpedia:EducationalInstitution");
		attributeValues.addElement("DBpedia:Eukaryote");
		attributeValues.addElement("DBpedia:Event");
		attributeValues.addElement("DBpedia:FictionalCharacter");
		attributeValues.addElement("DBpedia:Film");
		attributeValues.addElement("DBpedia:FloweringPlant");
		attributeValues.addElement("DBpedia:Food");
		attributeValues.addElement("DBpedia:Game");
		attributeValues.addElement("DBpedia:GovernmentAgency");
		attributeValues.addElement("DBpedia:Governor");
		attributeValues.addElement("DBpedia:Grape");
		attributeValues.addElement("DBpedia:Holiday");
		attributeValues.addElement("DBpedia:Infrastructure");
		attributeValues.addElement("DBpedia:Island");
		attributeValues.addElement("DBpedia:Language");
		attributeValues.addElement("DBpedia:Mammal");
		attributeValues.addElement("DBpedia:MilitaryPerson");
		attributeValues.addElement("DBpedia:MilitaryUnit");
		attributeValues.addElement("DBpedia:Model");
		attributeValues.addElement("DBpedia:Monarch");
		attributeValues.addElement("DBpedia:Mountain");
		attributeValues.addElement("DBpedia:MusicalArtist");
		attributeValues.addElement("DBpedia:MusicalWork");
		attributeValues.addElement("DBpedia:NaturalPlace");
		attributeValues.addElement("DBpedia:OfficeHolder");
		attributeValues.addElement("DBpedia:Organisation");
		attributeValues.addElement("DBpedia:Person");
		attributeValues.addElement("DBpedia:Philosopher");
		attributeValues.addElement("DBpedia:Place");
		attributeValues.addElement("DBpedia:Planet");
		attributeValues.addElement("DBpedia:Plant");
		attributeValues.addElement("DBpedia:PlayboyPlaymate");
		attributeValues.addElement("DBpedia:Politician");
		attributeValues.addElement("DBpedia:PopulatedPlace");
		attributeValues.addElement("DBpedia:ProgrammingLanguage");
		attributeValues.addElement("DBpedia:RecordLabel");
		attributeValues.addElement("DBpedia:Region");
		attributeValues.addElement("DBpedia:River");
		attributeValues.addElement("DBpedia:RouteOfTransportation");
		attributeValues.addElement("DBpedia:Royalty");
		attributeValues.addElement("DBpedia:Saint");
		attributeValues.addElement("DBpedia:Scientist");
		attributeValues.addElement("DBpedia:Settlement");
		attributeValues.addElement("DBpedia:Single");
		attributeValues.addElement("DBpedia:Software");
		attributeValues.addElement("DBpedia:SpaceMission");
		attributeValues.addElement("DBpedia:Species");
		attributeValues.addElement("DBpedia:Sport");
		attributeValues.addElement("DBpedia:Stream");
		attributeValues.addElement("DBpedia:Swimmer");
		attributeValues.addElement("DBpedia:TelevisionShow");
		attributeValues.addElement("DBpedia:TelevisionStation");
		attributeValues.addElement("DBpedia:University");
		attributeValues.addElement("DBpedia:Weapon");
		attributeValues.addElement("DBpedia:Website");
		attributeValues.addElement("DBpedia:Work");
		attributeValues.addElement("DBpedia:Writer");
		attributeValues.addElement("DBpedia:WrittenWork");
		attributeValues.addElement("Schema:AdministrativeArea");
		attributeValues.addElement("Schema:BodyOfWater");
		attributeValues.addElement("Schema:Book");
		attributeValues.addElement("Schema:City");
		attributeValues.addElement("Schema:CollegeOrUniversity");
		attributeValues.addElement("Schema:Continent");
		attributeValues.addElement("Schema:Country");
		attributeValues.addElement("Schema:CreativeWork");
		attributeValues.addElement("Schema:EducationalOrganization");
		attributeValues.addElement("Schema:Event");
		attributeValues.addElement("Schema:GovernmentOrganization");
		attributeValues.addElement("Schema:Language");
		attributeValues.addElement("Schema:Mountain");
		attributeValues.addElement("Schema:Movie");
		attributeValues.addElement("Schema:MusicAlbum");
		attributeValues.addElement("Schema:MusicGroup");
		attributeValues.addElement("Schema:Organization");
		attributeValues.addElement("Schema:Person");
		attributeValues.addElement("Schema:Place");
		attributeValues.addElement("Schema:Product");
		attributeValues.addElement("Schema:RiverBodyOfWater");
		attributeValues.addElement("Schema:TelevisionStation");
		attributeValues.addElement("Schema:WebPage");
		attributeValues.addElement("Misc");

		attribute = new Attribute("QueryResourceType", attributeValues);
		this.spotter = new Spotlight();
	}

	@Override
	public Object analyze(String q) {
		log.debug("String question: " + q);
		Map<String, List<Entity>> entities = null;
		try {
			entities = spotter.getEntities(q);
			if (!entities.isEmpty()) {
				for (Entity tmpEntity : entities.get("en")) {
					for (Resource type : tmpEntity.posTypesAndCategories) {
						return type.getURI();
					}
				}
			}
		} catch (IOException | ParseException e) {
			log.error("Annotator error");
		}
		return "Misc";

	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}

}
