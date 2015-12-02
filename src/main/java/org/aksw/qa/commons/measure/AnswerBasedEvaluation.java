package org.aksw.qa.commons.measure;

import java.util.Set;

import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnswerBasedEvaluation {
	static Logger log = LoggerFactory.getLogger(AnswerBasedEvaluation.class);

	public static double precision(Set<String> systemAnswer, Question question) {
		if (systemAnswer == null) {
			return 0;
		}
		double precision = 0;
		Set<String> goldenStrings = answersToString(question.goldenAnswers);
		if (question.pseudoSparqlQuery != null) {
			if (isSelectType(question.pseudoSparqlQuery)) {
				Set<String> intersection = CollectionUtils.intersection(goldenStrings, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.pseudoSparqlQuery)) {
				if (systemAnswer.size() == 1) {
					String ans = systemAnswer.iterator().next();
					String goldstandardAns = goldenStrings.iterator().next();
					if (ans.toString().equals(goldstandardAns.toString())) {
						precision = 1;
					}
				}
			} else {
				log.error("Unsupported Query Type" + question.pseudoSparqlQuery);
			}
		} else if (question.sparqlQuery != null) {
			if (isSelectType(question.sparqlQuery)) {
				Set<String> intersection = CollectionUtils.intersection(goldenStrings, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.sparqlQuery)) {
				if (systemAnswer.size() == 1) {
					String ans = systemAnswer.iterator().next();
					String goldstandardAns = goldenStrings.iterator().next();
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

	public static double recall(Set<String> systemAnswer, Question question) {
		if (systemAnswer == null) {
			return 0;
		}
		double recall = 0;
		Set<String> goldenStrings = answersToString(question.goldenAnswers);
		if (question.pseudoSparqlQuery != null) {
			if (isSelectType(question.pseudoSparqlQuery)) {
				// if queries contain aggregation return always 1
				if (question.aggregation) {
					recall = 1;
				}
				Set<String> intersection = CollectionUtils.intersection(systemAnswer, goldenStrings);
				if (goldenStrings.size() != 0) {
					recall = (double) intersection.size() / (double) goldenStrings.size();
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
				Set<String> intersection = CollectionUtils.intersection(systemAnswer, goldenStrings);
				if (goldenStrings.size() != 0) {
					recall = (double) intersection.size() / (double) goldenStrings.size();
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

	public static double fMeasure(Set<String> systemAnswers, Question question) {
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

	private static Set<String> answersToString(Set<String> answers) {
		Set<String> tmp = CollectionUtils.newHashSet();
		for (String s : answers) {
			tmp.add(s);
		}
		return tmp;
	}

}
