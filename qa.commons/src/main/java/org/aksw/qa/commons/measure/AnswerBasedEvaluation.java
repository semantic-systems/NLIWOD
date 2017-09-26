package org.aksw.qa.commons.measure;

import java.util.Set;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnswerBasedEvaluation {
	static Logger log = LoggerFactory.getLogger(AnswerBasedEvaluation.class);

	public static double precision(Set<String> systemAnswer, IQuestion question) {
		if (systemAnswer == null) {
			return 0;
		}
		double precision = 0;
		Set<String> goldenStrings = answersToString(question.getGoldenAnswers());
		if (question.getPseudoSparqlQuery() != null) {
			if (isSelectType(question.getPseudoSparqlQuery())) {
				Set<String> intersection = CollectionUtils.intersection(goldenStrings, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.getPseudoSparqlQuery())) {
				if (systemAnswer.size() == 1) {
					String ans = systemAnswer.iterator().next();
					String goldstandardAns = goldenStrings.iterator().next();
					if (ans.toString().equals(goldstandardAns.toString())) {
						precision = 1;
					}
				}
			} else {
				log.error("Unsupported Query Type" + question.getPseudoSparqlQuery());
			}
		} else if (question.getSparqlQuery() != null) {
			if (isSelectType(question.getSparqlQuery())) {
				Set<String> intersection = CollectionUtils.intersection(goldenStrings, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.getSparqlQuery())) {
				if (systemAnswer.size() == 1) {
					String ans = systemAnswer.iterator().next();
					String goldstandardAns = goldenStrings.iterator().next();
					if (ans.toString().equals(goldstandardAns.toString())) {
						precision = 1;
					}
				}
			} else {
				log.error("Unsupported Query Type" + question.getSparqlQuery());
			}
		}
		return precision;
	}

	public static double recall(Set<String> systemAnswer, IQuestion question) {
		if (systemAnswer == null) {
			return 0;
		}
		double recall = 0;
		Set<String> goldenStrings = answersToString(question.getGoldenAnswers());

		Boolean aggregation = question.getAggregation() == null ? false : question.getAggregation();
		if (question.getPseudoSparqlQuery() != null) {
			if (isSelectType(question.getPseudoSparqlQuery())) {
				// if queries contain aggregation return always 1
				if (aggregation) {
					recall = 1;
				}
				Set<String> intersection = CollectionUtils.intersection(systemAnswer, goldenStrings);
				if (goldenStrings.size() != 0) {
					recall = (double) intersection.size() / (double) goldenStrings.size();
				}
			} else if (isAskType(question.getPseudoSparqlQuery())) {
				// if queries are ASK queries return recall=1
				recall = 1;
			} else {
				log.error("Unsupported Query Type" + question.getPseudoSparqlQuery());
			}
		} else if (question.getSparqlQuery() != null) {
			if (isSelectType(question.getSparqlQuery())) {
				// if queries contain aggregation return always 1
				if (aggregation) {
					recall = 1;
				}
				Set<String> intersection = CollectionUtils.intersection(systemAnswer, goldenStrings);
				if (goldenStrings.size() != 0) {
					recall = (double) intersection.size() / (double) goldenStrings.size();
				}
			} else if (isAskType(question.getSparqlQuery())) {
				// if queries are ASK queries return recall=1
				recall = 1;
			} else {
				log.error("Unsupported Query Type" + question.getSparqlQuery());
			}
		}
		return recall;
	}

	public static double fMeasure(Set<String> systemAnswers, IQuestion question) {
		double precision = precision(systemAnswers, question);
		double recall = recall(systemAnswers, question);
		double fMeasure = 0;
		if (precision + recall > 0) {
			fMeasure = 2 * precision * recall / (precision + recall);
		}
		return fMeasure;
	}

	public static boolean isAskType(String sparqlQuery) {
		sparqlQuery = sparqlQuery.toUpperCase();
		return sparqlQuery.contains("\nASK\n") || sparqlQuery.contains("ASK ");
	}

	public static boolean isSelectType(String sparqlQuery) {
		sparqlQuery = sparqlQuery.toUpperCase();
		return sparqlQuery.contains("\nSELECT\n") || sparqlQuery.contains("SELECT ");
	}

	private static Set<String> answersToString(Set<String> answers) {
		Set<String> tmp = CollectionUtils.newHashSet();
		for (String s : answers) {
			tmp.add(s);
		}
		return tmp;
	}

}
