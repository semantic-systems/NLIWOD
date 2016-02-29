package org.aksw.hawk.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.hawk.datastructures.Question;
import org.aksw.qa.commons.load.QALD_Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class Baseline {
	static Logger log = LoggerFactory.getLogger(Baseline.class);
	String dataset;
	QALD_Loader datasetLoader;

	void run() throws IOException {
		List<Question> questions = datasetLoader.load(dataset);
		double overallf = 0;
		double overallp = 0;
		double overallr = 0;
		double counter = 0;
		for (Question q : questions) {
			if (q.answerType.equals("resource")) {
				if (q.onlydbo) {
					if (!q.aggregation) {
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

	public Map<String, Set<RDFNode>> calculateSPARQLRepresentation(Question q) {
		Map<String, Set<RDFNode>> answer = Maps.newHashMap();
		return answer;
	}

	public static void main(String args[]) throws IOException {

		for (String file : new String[] { "resources/qald-4_hybrid_train.xml" }) { // ,"resources/qald-4_multilingual_train_withanswers.xml"
			Baseline controller = new Baseline();

			log.info("Configuring controller");

			controller.dataset = new File(file).getAbsolutePath();
			controller.datasetLoader = new QALD_Loader();

			log.info("Run controller");
			controller.run();
		}
	}
}