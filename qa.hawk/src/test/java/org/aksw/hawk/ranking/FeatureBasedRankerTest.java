package org.aksw.hawk.ranking;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.hawk.ranking.FeatureBasedRanker.Feature;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class FeatureBasedRankerTest {

	@Test
	// TODO Christian: transform this to unit test (not working yet, ranking not
	// implemented)
	@Ignore
	public void test() {

		List<SPARQLQuery> queries = Lists.newArrayList();
		// Which actress starring in the TV series Friends owns the production
		// company Coquette Productions?
		SPARQLQuery query = new SPARQLQuery("?const <http://dbpedia.org/ontology/starring> ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "Friends");
		queries.add(query);

		query = new SPARQLQuery("?const ?verb ?proj.");
		query.addFilterOverAbstractsContraint("?proj", "Coquette Productions");
		query.addConstraint("?proj <http://dbpedia.org/ontology/owner> ?const");
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
				HAWKQuestion quest = new HAWKQuestion();
				// System.out.println(queries);

				List<Answer> answers = Lists.newArrayList();
				for (SPARQLQuery q : queries) {
					answers.add(q.toAnswer());
				}
				System.out.println("answers:");
				System.out.println(answers);

				List<Answer> rankedanswers = ranker.rank(answers, quest);
				System.out.println(rankedanswers);
				List<SPARQLQuery> returnqueries = Lists.newArrayList();
				for (Answer ans : rankedanswers) {
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
