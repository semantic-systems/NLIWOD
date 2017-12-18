package org.aksw.hawk.experiment;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.aksw.hawk.controller.AbstractPipeline;
import org.aksw.hawk.controller.EvalObj;
import org.aksw.hawk.controller.PipelineStanford;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.aksw.qa.commons.sparql.SPARQLQuery;
import org.apache.log4j.Logger;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

public class QALD7_Multilingual_Train_Pipeline {
	private static Logger log = Logger.getLogger(QALD7_Multilingual_Train_Pipeline.class);

	public QALD7_Multilingual_Train_Pipeline() throws ExecutionException, RuntimeException {

		log.info("Configuring controller");
		AbstractPipeline pipeline = new PipelineStanford();

		log.info("Loading dataset");
		List<HAWKQuestion> questions = null;

		// questions =
		// HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD7_Train_Hybrid));
		questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD7_Train_Multilingual));

		double average = 0;
		double count = 0;
		double countNULLAnswer = 0;
		questions.sort((HAWKQuestion o1, HAWKQuestion o2) -> o1.getLanguageToQuestion().get("en").length()
				- o2.getLanguageToQuestion().get("en").length());

		for (HAWKQuestion q : questions) {
			System.gc();
			if (q.checkSuitabillity()) {
				log.info("Run pipeline on " + count + ":" + q.getLanguageToQuestion().get("en"));
				List<Answer> answers = pipeline.getAnswersToQuestion(q);

				if (answers.isEmpty()) {
					log.warn("Question#" + q.getId() + " returned no answers! (Q: "
							+ q.getLanguageToQuestion().get("en") + ")");
					++countNULLAnswer;
					continue;
				}
				++count;

				// optimal ranking
				log.info("Optimal ranking");
				int maximumPositionToMeasure = 1000;
				List<EvalObj> eval = Measures.measure(answers, q, maximumPositionToMeasure);
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
				// System.out.println("Max F-measure: " + fmax);
				average += fmax;
				// log.info("Feature-based ranking begins training.");
				// feature_ranker.learn(q, queries);
			}
		}

		log.info("Number of questions with answer: " + count + ", number of questions without answer: "
				+ countNULLAnswer);
		log.info("Average F-measure: " + (average / count));
	}

	// TODO When HAWK is fast enough change to unit test
	public static void main(String args[]) throws ExecutionException, RuntimeException {
		new QALD7_Multilingual_Train_Pipeline();

	}

}
