package org.aksw.hawk.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class Baseline {
	static Logger log = LoggerFactory.getLogger(Baseline.class);
	String dataset;

	void run(final Dataset dataset) throws IOException {
		List<HAWKQuestion> questions = HAWKQuestionFactory.createInstances(QALD_Loader.load(dataset));
		double overallf = 0;
		double overallp = 0;
		double overallr = 0;
		double counter = 0;
		// TODO refactor this with proper check for HAWK abilities
		for (HAWKQuestion q : questions) {
			if (q.getAnswerType().equals("resource")) {
				if (q.getOnlydbo()) {
					if (!q.getAggregation()) {
						Map<String, Set<RDFNode>> answer = calculateSPARQLRepresentation(q);

						double fmax = 0;
						double pmax = 0;
						double rmax = 0;
						for (String query : answer.keySet()) {
							Set<RDFNode> systemAnswers = answer.get(query);
							double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
							double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
							double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
							if (fMeasure > fmax) {
								log.info(query.substring(0, Math.min(1000, query.length())));
								log.info("\tP=" + precision + " R=" + recall + " F=" + fMeasure);
								fmax = fMeasure;
								pmax = precision;
								rmax = recall;
							}
						}
						overallf += fmax;
						overallp += pmax;
						overallr += rmax;
						counter++;
						log.info("########################################################");
					}
				}
			}
		}
		log.info("Average P=" + overallp / counter + " R=" + overallr / counter + " F=" + overallf / counter + " Counter=" + counter);
	}

	public Map<String, Set<RDFNode>> calculateSPARQLRepresentation(final HAWKQuestion q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();
		return answer;
	}

	public static void main(final String args[]) throws IOException {

		log.info("Configuring controller");

		Baseline controller = new Baseline();
		Dataset dataset = Dataset.QALD5_Train_Hybrid;
		log.info("Run controller");

		controller.run(dataset);
	}
}