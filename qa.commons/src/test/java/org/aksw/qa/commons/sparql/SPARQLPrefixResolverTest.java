package org.aksw.qa.commons.sparql;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPARQLPrefixResolverTest {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Test
	public void prefixResolverTest() {
		String prefixQuery = "SELECT DISTINCT ?uri WHERE { ?uri a dbo:Musical . ?uri dbo:musicBy <http://dbpedia.org/resource/Elton_John> .}";

		
		String realAnswer = "PREFIX  dbo:  <http://dbpedia.org/ontology/>\n" + 
				"PREFIX  res:  <http://dbpedia.org/resource/>\n" + 
				"PREFIX  rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"\n" + 
				"SELECT DISTINCT  ?uri\n" + 
				"WHERE\n" + 
				"  { ?uri  rdf:type     dbo:Musical ;\n" + 
				"          dbo:musicBy  res:Elton_John\n" + 
				"  }\n";
				
		String answer = SPARQLPrefixResolver.addMissingPrefixes(prefixQuery);

		Assert.assertTrue("Prefixes not correctly resolved.", realAnswer.equals(answer));
		log.debug("Query with resolved Prefixes: \n" + answer);
	}
}
