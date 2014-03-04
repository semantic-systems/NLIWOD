package hawk;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.Question;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class EvaluationUtilTest {
	Logger log = LoggerFactory.getLogger(EvaluationUtilTest.class);

	@Test
	public void askTestTrue() {
		Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
		systemAnswers.add(new ResourceImpl("true"));

		Question q = new Question();
		q.sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/>\nASK\nWHERE {	?m1 text:\"exhibits\" res:Rosetta_Stone .		res:Gayer-Anderson_Cat dbo:museum ?x .        ?m2 rdfs:label ?m2 .        FILTER (?m1=?m2)}";
		q.goldenAnswers.add("true");
		q.aggregation = true;
		double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
		double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
		double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
		log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
		assertTrue(precision == 1 && recall == 1 && fMeasure == 1);
	}

	@Test
	public void askTestFalse() {
		Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
		systemAnswers.add(new ResourceImpl("false"));

		Question q = new Question();
		q.sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/>\nASK\nWHERE {	?m1 text:\"exhibits\" res:Rosetta_Stone .		res:Gayer-Anderson_Cat dbo:museum ?x .        ?m2 rdfs:label ?m2 .        FILTER (?m1=?m2)}";
		q.goldenAnswers.add("true");
		q.aggregation = true;
		double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
		double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
		double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
		log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
		assertTrue(precision == 0 && recall == 1 && fMeasure == 0);
	}

	@Test
	public void selectTestNumber() {
		Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
		systemAnswers.add(new ResourceImpl("25"));

		Question q = new Question();
		q.sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/>\nSELECT\n?n WHERE {         res:Steve_Jobs dbo:relative ?uri .         ?uri text:\"did not meet until she was\" ?n .}";
		q.goldenAnswers.add("25");
		q.aggregation = true;
		double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
		double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
		double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
		log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
		assertTrue(precision == 1 && recall == 1 && fMeasure == 1);
	}

	@Test
	public void selectTestNumberFalseAnswer() {
		Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
		systemAnswers.add(new ResourceImpl("3"));

		Question q = new Question();
		q.sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/>\nSELECT\n?n WHERE {         res:Steve_Jobs dbo:relative ?uri .         ?uri text:\"did not meet until she was\" ?n .}";
		q.goldenAnswers.add("25");
		q.aggregation = true;
		double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
		double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
		double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
		log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
		assertTrue(precision == 0 && recall == 0 && fMeasure == 0);
	}

	@Test
	public void selectTestResource() {
		Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
		systemAnswers.add(new ResourceImpl("http://dbpedia.org/resource/Ghostface_Killah"));
		systemAnswers.add(new ResourceImpl("http://dbpedia.org/resource/Method_Man"));
		
		Question q = new Question();
		q.sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri  WHERE {         res:Wu-Tang_Clan dbo:bandMember ?uri .         ?uri text:\"stage name\" ?x .         ?x text:\"from\" text:\"movie\" . }";
		q.goldenAnswers.add("http://dbpedia.org/resource/Ghostface_Killah");
		q.goldenAnswers.add("http://dbpedia.org/resource/Method_Man");
		q.aggregation = true;
		double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
		double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
		double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
		log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
		assertTrue(precision == 1 && recall == 1 && fMeasure == 1);
	}

	@Test
	public void selectTestResourceFalse() {
		Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
		systemAnswers.add(new ResourceImpl("http://dbpedia.org/resource/Ghostface_Killah"));
		systemAnswers.add(new ResourceImpl("http://dbpedia.org/resource/Barack Obama"));
		
		Question q = new Question();
		q.sparqlQuery = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri  WHERE {         res:Wu-Tang_Clan dbo:bandMember ?uri .         ?uri text:\"stage name\" ?x .         ?x text:\"from\" text:\"movie\" . }";
		q.goldenAnswers.add("http://dbpedia.org/resource/Ghostface_Killah");
		q.goldenAnswers.add("http://dbpedia.org/resource/Method_Man");
		q.aggregation = true;
		double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
		double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
		double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
		log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
		assertTrue(precision == 0.5 && recall == 0.5 && fMeasure == 0.5);
	}
}
