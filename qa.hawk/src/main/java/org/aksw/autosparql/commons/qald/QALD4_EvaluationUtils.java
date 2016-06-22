package org.aksw.autosparql.commons.qald;

import java.util.HashSet;
import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class QALD4_EvaluationUtils {
	static Logger log = LoggerFactory.getLogger(QALD4_EvaluationUtils.class);

	public static double precision(final Set<RDFNode> systemAnswer, final HAWKQuestion question) {
		if (systemAnswer == null) {
			return 0;
		}
		double precision = 0;
		Set<RDFNode> goldenRDFNodes = answersToRDFNode(question.getGoldenAnswers());
		if (question.getPseudoSparqlQuery() != null) {
			if (isSelectType(question.getPseudoSparqlQuery())) {
				SetView<RDFNode> intersection = Sets.intersection(goldenRDFNodes, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.getPseudoSparqlQuery())) {
				if (systemAnswer.size() == 1) {
					RDFNode ans = systemAnswer.iterator().next();
					RDFNode goldstandardAns = goldenRDFNodes.iterator().next();
					if (ans.toString().equals(goldstandardAns.toString())) {
						precision = 1;
					}
				}
			} else {
				log.error("Unsupported Query Type" + question.getPseudoSparqlQuery());
			}
		} else if (question.getSparqlQuery() != null) {
			if (isSelectType(question.getSparqlQuery())) {
				SetView<RDFNode> intersection = Sets.intersection(goldenRDFNodes, systemAnswer);
				if (systemAnswer.size() != 0) {
					precision = (double) intersection.size() / (double) systemAnswer.size();
				}
			} else if (isAskType(question.getSparqlQuery())) {
				if (systemAnswer.size() == 1) {
					RDFNode ans = systemAnswer.iterator().next();
					RDFNode goldstandardAns = goldenRDFNodes.iterator().next();
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

	public static double recall(final Set<RDFNode> systemAnswer, final HAWKQuestion question) {
		if (systemAnswer == null) {
			return 0;
		}
		double recall = 0;
		Set<RDFNode> goldenRDFNodes = answersToRDFNode(question.getGoldenAnswers());
		if (question.getPseudoSparqlQuery() != null) {
			if (isSelectType(question.getPseudoSparqlQuery())) {
				// if queries contain aggregation return always 1
				if (question.getAggregation()) {
					recall = 1;
				}
				SetView<RDFNode> intersection = Sets.intersection(systemAnswer, goldenRDFNodes);
				if (goldenRDFNodes.size() != 0) {
					recall = (double) intersection.size() / (double) goldenRDFNodes.size();
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
				if (question.getAggregation()) {
					recall = 1;
				}
				SetView<RDFNode> intersection = Sets.intersection(systemAnswer, goldenRDFNodes);
				if (goldenRDFNodes.size() != 0) {
					recall = (double) intersection.size() / (double) goldenRDFNodes.size();
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

	public static double fMeasure(final Set<RDFNode> systemAnswers, final HAWKQuestion question) {
		double precision = precision(systemAnswers, question);
		double recall = recall(systemAnswers, question);
		double fMeasure = 0;
		if (precision + recall > 0) {
			fMeasure = 2 * precision * recall / (precision + recall);
		}
		return fMeasure;
	}

	public static boolean isAskType(final String sparqlQuery) {
		if (sparqlQuery == null) {
			return false;
		}
		return sparqlQuery.contains("\nASK\n") || sparqlQuery.contains("ASK ");
	}

	private static boolean isSelectType(final String sparqlQuery) {
		return sparqlQuery.contains("\nSELECT\n") || sparqlQuery.contains("SELECT ");
	}

	private static Set<RDFNode> answersToRDFNode(final Set<String> answers) {
		Set<RDFNode> tmp = new HashSet<>();
		for (String s : answers) {
			tmp.add(new ResourceImpl(s));
		}
		return tmp;
	}

}
