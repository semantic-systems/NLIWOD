package org.aksw.qa.commons.measure;

import java.util.HashSet;
import java.util.Set;

import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.utils.CollectionUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO rename class to AnswerBasedEvaluation and the other to SPARQLBasedEvaluation
public class HybridEvaluation {
	static Logger log = LoggerFactory.getLogger(HybridEvaluation.class);

	public static double precision(Set<RDFNode> systemAnswer, Question question) {
		if (systemAnswer == null) {
			return 0;
		}
		double precision = 0;
		Set<RDFNode> goldenRDFNodes = answersToRDFNode(question.goldenAnswers);
		if (question.pseudoSparqlQuery != null) {
			if (isSelectType(question.pseudoSparqlQuery)) {
				Set<RDFNode> intersection = CollectionUtils.intersection(goldenRDFNodes, systemAnswer);
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
				Set<RDFNode> intersection = CollectionUtils.intersection(goldenRDFNodes, systemAnswer);
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
		Set<RDFNode> goldenRDFNodes = answersToRDFNode(question.goldenAnswers);
		if (question.pseudoSparqlQuery != null) {
			if (isSelectType(question.pseudoSparqlQuery)) {
				// if queries contain aggregation return always 1
				if (question.aggregation) {
					recall = 1;
				}
				Set<RDFNode> intersection = CollectionUtils.intersection(systemAnswer, goldenRDFNodes);
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
				Set<RDFNode> intersection = CollectionUtils.intersection(systemAnswer, goldenRDFNodes);
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

	// TODO discard this once Jena has been removed from this project
	public static double fMeasureString(Set<String> systemAnswersString, Question question) {
		Set<RDFNode> systemAnswers = Sets.newHashSet();
		for (String answer : systemAnswersString) {
			systemAnswers.add(new ResourceImpl(answer));
		}
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
