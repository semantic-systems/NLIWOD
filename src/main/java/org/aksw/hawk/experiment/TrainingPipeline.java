package org.aksw.hawk.experiment;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.controller.EvalObj;
import org.aksw.hawk.controller.Pipeline;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.hawk.ranking.FeatureBasedRanker;
import org.aksw.hawk.ranking.OptimalRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * F@N + all ranking experiments for ESWC 2015 publication Possibly extendible for testing NER things
 * 
 * @author Lorenz Buehmann
 * @author ricardousbeck
 * 
 */
public class TrainingPipeline {
	static Logger log = LoggerFactory.getLogger(TrainingPipeline.class);

	public static void main(String args[]) throws IOException, ParserConfigurationException {
		log.info("Configuring controller");
		Pipeline pipeline = new Pipeline();

		log.info("Loading dataset");
		String dataset = "resources/qald-5_test_train.xml";
		List<Question> questions = QALD_Loader.load(dataset);

		double average = 0;
		double count = 0;
		for (Question q : questions) {
			if (q.hybrid & q.answerType.equals("resource") & q.onlydbo & !q.aggregation) {
				count++;
				log.info("Run pipeline on " + q.languageToQuestion.get("en"));
				List<Answer> answers = pipeline.getAnswersToQuestion(q);

				// ##############~~RANKING~~##############
				log.info("Run ranking");
				int maximumPositionToMeasure = 10;
				OptimalRanker optimal_ranker = new OptimalRanker();
				FeatureBasedRanker feature_ranker = new FeatureBasedRanker();

				// optimal ranking
				log.info("Optimal ranking");
				List<Answer> rankedAnswer = optimal_ranker.rank(answers, q);
				List<EvalObj> eval = Measures.measure(rankedAnswer, q, maximumPositionToMeasure);
				log.debug(Joiner.on("\n\t").join(eval));

				Set<SPARQLQuery> queries = Sets.newHashSet();
				double fmax = 0;
				for (EvalObj e : eval) {
					if (e.getFmax() == fmax) {
						queries.add(e.getAnswer().query);
					} else if (e.getFmax() > fmax) {
						queries.clear();
						queries.add(e.getAnswer().query);
						fmax = e.getFmax();
					}
				}
				log.info("Max F-measure: " + fmax);
				average += fmax;
				log.info("Feature-based ranking begins training.");
				feature_ranker.learn(q, queries);
			}
		}
		log.info("Average F-measure: " + (average / count));

	}
}
