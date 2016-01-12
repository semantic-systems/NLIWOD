package org.aksw.hawk.ranking;

import static org.junit.Assert.*;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.controller.Pipeline;
import org.aksw.hawk.pruner.disjointness.DisjointnessBasedQueryFilter;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.hawk.ranking.FeatureBasedRanker.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.rdf.model.RDFNode;


public class FeatureBasedRankerTest {

	@Test
	public void test() {
		// TODO Christian: transform this to unit test
		
		 List<SPARQLQuery> queries = Lists.newArrayList();
		
		 SPARQLQuery query = new SPARQLQuery("?const <http://dbpedia.org/ontology/starring> ?proj.");
		 query.addFilterOverAbstractsContraint("?proj", "Coquette Productions");
		 queries.add(query);
		
		 query = new SPARQLQuery("?const ?verb ?proj.");
		 query.addFilterOverAbstractsContraint("?proj", "Coquette Productions");
		 query.addConstraint("?proj <http://dbpedia.org/ontology/birthPlace> ?const");
		 queries.add(query);
		 System.out.println("queries:");
		 System.out.println(queries);
		 FeatureBasedRanker ranker = new FeatureBasedRanker();
		 Logger logger = LoggerFactory.getLogger(FeatureBasedRanker.class);
		 for (Set<Feature> featureSet : Sets.powerSet(new HashSet<>(Arrays.asList(Feature.values())))) {
			 if (!featureSet.isEmpty()) {
				 logger.debug("Feature-based ranking: " + featureSet.toString());
				 ranker.setFeatures(featureSet);

				 ranker.train();
				 Question quest = new Question();
				 //System.out.println(queries);


				 List<Answer> answers=Lists.newArrayList();
				 for (SPARQLQuery q: queries){
					 answers.add(q.toAnswer());
				 }
				 System.out.println("answers:");
				 System.out.println(answers);

				 List<Answer> rankedanswers=ranker.rank(answers, quest);
				 System.out.println(rankedanswers);
				 List<SPARQLQuery> returnqueries = Lists.newArrayList();
				 for (Answer ans: rankedanswers){
					 returnqueries.add(ans.toSPARQLQuery());
				 }
				 for (SPARQLQuery q : returnqueries) {
					 logger.debug(q.toString());
				 }
				 System.out.println("rankedanswers:");
				 System.out.println(rankedanswers);
				 System.out.println("returnqueries:");
				 System.out.println(returnqueries);
			 }
		 }
	}

}
