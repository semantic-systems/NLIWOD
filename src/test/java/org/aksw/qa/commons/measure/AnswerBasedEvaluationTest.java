package org.aksw.qa.commons.measure;

import static org.junit.Assert.assertEquals;
import java.util.Set;

import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.utils.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnswerBasedEvaluationTest {

	Logger logger = LoggerFactory.getLogger(AnswerBasedEvaluationTest.class);


	@Test
	public void testTooGenericQuery() {
		
		Set<String> systemAnswer=tooGenericAnswers();
		Question question = getQuestion();
		double precision = AnswerBasedEvaluation.precision(systemAnswer, question);
		double recall = AnswerBasedEvaluation.recall(systemAnswer, question);
		double fMeasure = AnswerBasedEvaluation.fMeasure(systemAnswer, question);
		
		assertEquals(0.444, precision, 0.001);
		assertEquals(1.0, recall, 0.0);
		assertEquals(0.615, fMeasure, 0.001);

		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		logger.debug("P=" + precision);
		logger.debug("R=" + recall);
		logger.debug("F=" + fMeasure);

	}

	@Test
	public void testTooSpecificQuery() {
		Set<String> systemAnswer=tooSpecificAnswers();
		Question question = getQuestion();
		double precision = AnswerBasedEvaluation.precision(systemAnswer, question);
		double recall = AnswerBasedEvaluation.recall(systemAnswer, question);
		double fMeasure = AnswerBasedEvaluation.fMeasure(systemAnswer, question);
		
		assertEquals(1, precision, 0);
		assertEquals(0.75, recall, 0.0);
		assertEquals(0.857, fMeasure, 0.001);

		logger.debug(Thread.currentThread().getStackTrace()[1].getMethodName());
		logger.debug("P=" + precision);
		logger.debug("R=" + recall);
		logger.debug("F=" + fMeasure);
	}

	//TODO Generate AnswerSets and Questions
	//Question
	private Set<String> tooGenericAnswers(){
		Set<String> ret = CollectionUtils.newHashSet();
		//Generic SPARQL: "SELECT ?pers {?pers rdf:type <http://dbpedia.org/prop/worker> }"
		ret.add("http://dbpedia.org/WorkerA");
		ret.add("http://dbpedia.org/WorkerB");
		ret.add("http://dbpedia.org/WorkerC");
		ret.add("http://dbpedia.org/WorkerD");
		ret.add("http://dbpedia.org/WorkerE");
		ret.add("http://dbpedia.org/WorkerF");
		ret.add("http://dbpedia.org/ManagerA");
		ret.add("http://dbpedia.org/ManagerB");
		ret.add("http://dbpedia.org/ManagerC");
		return ret;
	}
	
	private Set<String> tooSpecificAnswers(){
		Set<String> ret = CollectionUtils.newHashSet();
		//Generic SPARQL: "SELECT ?pers {?pers rdf:type <http://dbpedia.org/prop/worker> .
		// ?pers <http://dbpedia.org/prop/division> <http://dbpedia.org/DivisionA> .
		// ?pers <http://dbpedia.org/prop/job> <http://dbpedia.org/employee>}"
		ret.add("http://dbpedia.org/WorkerA");
		ret.add("http://dbpedia.org/WorkerB");
		ret.add("http://dbpedia.org/WorkerC");
		return ret;
	}
	
	private Question getQuestion(){
		Question q =  new Question();
		q.aggregation=false;
		q.pseudoSparqlQuery="SELECT ?pers {?pers <http://dbpedia.org/prop/division> <http://dbpedia.org/DivisionA> . "
				+ " ?pers rdf:type <http://dbpedia.org/prop/worker> }";
		q.goldenAnswers=getGoldenAnswers();
		return q;
	}

	
	private Set<String> getGoldenAnswers(){
		//Golden Answer
		Set<String> ret = CollectionUtils.newHashSet();
		ret.add("http://dbpedia.org/WorkerA");
		ret.add("http://dbpedia.org/WorkerB");
		ret.add("http://dbpedia.org/WorkerC");
		ret.add("http://dbpedia.org/ManagerA");
		return ret;
	}
	
}
