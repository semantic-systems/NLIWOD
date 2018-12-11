package org.aksw.mlqa.analyzer.querytype;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.aksw.mlqa.analyzer.IAnalyzer;
import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;
import org.apache.jena.rdf.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import weka.core.Attribute;

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
		ArrayList<String> attributeValues = new ArrayList<String>();
		// attributeValues.add("Place");
		// attributeValues.add("Person");
		// attributeValues.add("Organization");
		// attributeValues.add("Misc");

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
		attributeValues.add("DBpedia:Person");
		attributeValues.add("DBpedia:Philosopher");
		attributeValues.add("DBpedia:Place");
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
		attributeValues.add("Misc");

		attribute = new Attribute("QueryResourceType", attributeValues);
		this.spotter = new Spotlight();
	}

	@Override
	public Object analyze(String q) {
		log.debug("String question: " + q);
		Map<String, List<Entity>> entities = null;
		entities = spotter.getEntities(q);
		if (!entities.isEmpty()) {
			for (Entity tmpEntity : entities.get("en")) {
				for (Resource type : tmpEntity.getPosTypesAndCategories()) {
					return type.getURI();
				}
			}
		}
		return "Misc";

	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
