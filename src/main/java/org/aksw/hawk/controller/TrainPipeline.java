package org.aksw.hawk.controller;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

public class TrainPipeline extends Pipeline {
	static Logger log = LoggerFactory.getLogger(TrainPipeline.class);

	public static void main(String args[]) throws IOException {

		Set<EvalObj> evals = Sets.newHashSet();

		log.info("Configuring controller");
		TrainPipeline controller = new TrainPipeline();

		log.info("Run controller");
		for (String file : new String[] { "resources/qald-4_hybrid_train.xml", "resources/qald-4_hybrid_test_withanswers.xml" }) { // test_withanswers
			controller.dataset = new File(file).getAbsolutePath();
			controller.run(evals);
		}
		log.info("Writing results");
		controller.write(evals);
	}

	@Override
	Map<String, Answer> buildQuery(Question q) {
		// build queries via ranking
		Map<String, Answer> answer = queryBuilder.build(q, ranker);
		return answer;
	}

	@Override
	void modus(Question q, Set<SPARQLQuery> queries) {
		this.ranker.learn(q, queries);
	}
}