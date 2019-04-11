package org.aksw.qa.commons.qald;

import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Assert;
import org.junit.Test;



public class TriplePatternExtractorTest {
	
	@Test
	public void extractProjectionVarsTest() {
		Query q = QueryFactory.create("prefix  dbp:  <http://dbpedia.org/resource/> " + "prefix  dbp2: <http://dbpedia.org/ontology/> " + "select  ?x where  { dbp:total dbp2:thumbnail ?thumbnail. "
		        		+ "?thumbnail dbp2:thumbnail ?x. ?y dbp2:thumbnail ?c}");
		
		String realAnswer = "[?thumbnail @http://dbpedia.org/ontology/thumbnail ?x]";
		
		TriplePatternExtractor triplePatternExtractor = new TriplePatternExtractor();
		Map<Var,Set<Triple>> answer = triplePatternExtractor.extractTriplePatternsForProjectionVars(q);
		Set<Triple> extractedTriple = answer.get(q.getProjectVars().get(0));
		
		Assert.assertTrue(realAnswer.equals(extractedTriple.toString()));
	}
	
	@Test
	public void extractTripleTest() {
		Query q = QueryFactory.create("prefix  dbp:  <http://dbpedia.org/resource/> " + "prefix  dbp2: <http://dbpedia.org/ontology/> " + "select  ?y ?c where  { dbp:total dbp2:thumbnail ?thumbnail. "
		        		+ "?thumbnail dbp2:thumbnail ?y. ?x dbp2:thumbnail ?c}");
		
		String realAnswer = "[?x @http://dbpedia.org/ontology/thumbnail ?c]";
		
		TriplePatternExtractor triplePatternExtractor = new TriplePatternExtractor();
		Set<Triple> answer = triplePatternExtractor.extractTriplePatterns(q, q.getProjectVars().get(1));
		Assert.assertTrue(realAnswer.equals(answer.toString()));
	}
	
}
