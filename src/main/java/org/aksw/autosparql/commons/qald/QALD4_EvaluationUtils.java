package org.aksw.autosparql.commons.qald;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class QALD4_EvaluationUtils {
	static Logger log = LoggerFactory.getLogger(QALD4_EvaluationUtils.class);

	public static double precision(Set<RDFNode> systemAnswer, Question question) {
		if (systemAnswer == null) {
			return 0;
		}
		double precision = 0;
		Set<RDFNode> goldenRDFNodes = answersToRDFNode(question.goldenAnswers.get("en"));
		if (question.pseudoSparqlQuery != null) {
			if (isSelectType(question.pseudoSparqlQuery)) {
				SetView<RDFNode> intersection = Sets.intersection(goldenRDFNodes, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.pseudoSparqlQuery)) {
				if (systemAnswer.size() == 1) {
					RDFNode ans = systemAnswer.iterator().next();
					RDFNode goldstandardAns = goldenRDFNodes.iterator().next();
					if (ans.toString().equals(goldstandardAns.toString())) {
						precision = 1;
					}
				}
			} else {
				log.error("Unsupported Query Type" + question.pseudoSparqlQuery);
			}
		} else if (question.sparqlQuery != null) {
			if (isSelectType(question.sparqlQuery)) {
				SetView<RDFNode> intersection = Sets.intersection(goldenRDFNodes, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.sparqlQuery)) {
				if (systemAnswer.size() == 1) {
					RDFNode ans = systemAnswer.iterator().next();
					RDFNode goldstandardAns = goldenRDFNodes.iterator().next();
					if (ans.toString().equals(goldstandardAns.toString())) {
						precision = 1;
					}
				}
			} else {
				log.error("Unsupported Query Type" + question.sparqlQuery);
			}
		}
		return precision;
	}

	public static double recall(Set<RDFNode> systemAnswer, Question question) {
		if (systemAnswer == null) {
			return 0;
		}
		double recall = 0;
		Set<RDFNode> goldenRDFNodes = answersToRDFNode(question.goldenAnswers.get("en"));
		if (question.pseudoSparqlQuery != null) {
			if (isSelectType(question.pseudoSparqlQuery)) {
				// if queries contain aggregation return always 1
				if (question.aggregation) {
					recall = 1;
				}
				SetView<RDFNode> intersection = Sets.intersection(systemAnswer, goldenRDFNodes);
				if (goldenRDFNodes.size() != 0) {
					recall = (double) intersection.size() / (double) goldenRDFNodes.size();
				}
			} else if (isAskType(question.pseudoSparqlQuery)) {
				// if queries are ASK queries return recall=1
				recall = 1;
			} else {
				log.error("Unsupported Query Type" + question.pseudoSparqlQuery);
			}
		} else if (question.sparqlQuery != null) {
			if (isSelectType(question.sparqlQuery)) {
				// if queries contain aggregation return always 1
				if (question.aggregation) {
					recall = 1;
				}
				SetView<RDFNode> intersection = Sets.intersection(systemAnswer, goldenRDFNodes);
				if (goldenRDFNodes.size() != 0) {
					recall = (double) intersection.size() / (double) goldenRDFNodes.size();
				}
			} else if (isAskType(question.sparqlQuery)) {
				// if queries are ASK queries return recall=1
				recall = 1;
			} else {
				log.error("Unsupported Query Type" + question.sparqlQuery);
			}
		}
		return recall;
	}

	public static double fMeasure(Set<RDFNode> systemAnswers, Question question) {
		double precision = precision(systemAnswers, question);
		double recall = recall(systemAnswers, question);
		double fMeasure = 0;
		if (precision + recall > 0) {
			fMeasure = 2 * precision * recall / (precision + recall);
		}
		return fMeasure;
	}

	private static boolean isAskType(String sparqlQuery) {
		return sparqlQuery.contains("\nASK\n") || sparqlQuery.contains("ASK ");
	}

	private static boolean isSelectType(String sparqlQuery) {
		return sparqlQuery.contains("\nSELECT\n") || sparqlQuery.contains("SELECT ");
	}

	private static Set<RDFNode> answersToRDFNode(Set<String> answers) {
		Set<RDFNode> tmp = new HashSet<RDFNode>();
		for (String s : answers) {
			tmp.add(new ResourceImpl(s));
		}
		return tmp;
	}

}
