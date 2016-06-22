package hawk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO christian change to Evaluation Util from qa-commons
public class EvaluationUtilTest {
	Logger log = LoggerFactory.getLogger(EvaluationUtilTest.class);
	//
	// @Test
	// public void askTestTrue() {
	// Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
	// systemAnswers.add(new ResourceImpl("true"));
	//
	// Question q = new Question();
	// q.pseudoSparqlQuery =
	// "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/>\nASK {	?m1 text:\"exhibits\" res:Rosetta_Stone .		res:Gayer-Anderson_Cat dbo:museum ?x .        ?m2 rdfs:label ?m2 .        FILTER (?m1=?m2)}";
	// HashSet<String> set = new HashSet<>();
	// set.add("true");
	// q.goldenAnswers.put("en", set);
	// q.aggregation = true;
	// double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
	// double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
	// double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
	// log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
	// assertTrue(precision == 1 && recall == 1 && fMeasure == 1);
	// }
	//
	// @Test
	// public void askTestFalse() {
	// Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
	// systemAnswers.add(new ResourceImpl("false"));
	//
	// Question q = new Question();
	// q.pseudoSparqlQuery =
	// "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/>\nASK {	?m1 text:\"exhibits\" res:Rosetta_Stone .		res:Gayer-Anderson_Cat dbo:museum ?x .        ?m2 rdfs:label ?m2 .        FILTER (?m1=?m2)}";
	// HashSet<String> set = new HashSet<>();
	// set.add("true");
	// q.goldenAnswers.put("en", set);
	// q.aggregation = true;
	// double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
	// double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
	// double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
	// log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
	// assertTrue(precision == 0 && recall == 1 && fMeasure == 0);
	// }
	//
	// @Test
	// public void selectTestNumber() {
	// Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
	// systemAnswers.add(new ResourceImpl("25"));
	//
	// Question q = new Question();
	// q.setPseudoSparqlQuery("PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/>\nSELECT\n?n WHERE {         res:Steve_Jobs dbo:relative ?uri .         ?uri text:\"did not meet until she was\" ?n .}");
	// HashSet<String> set = new HashSet<>();
	// set.add("25");
	// q.getGoldenAnswers().put(set);
	// q.setAggregation(true);
	// double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
	// double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
	// double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
	// log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
	// assertTrue(precision == 1 && recall == 1 && fMeasure == 1);
	// }
	//
	// @Test
	// public void selectTestNumberFalseAnswer() {
	// Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
	// systemAnswers.add(new ResourceImpl("3"));
	//
	// Question q = new Question();
	// q.pseudoSparqlQuery =
	// "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/>\nSELECT\n?n WHERE {         res:Steve_Jobs dbo:relative ?uri .         ?uri text:\"did not meet until she was\" ?n .}";
	// HashSet<String> set = new HashSet<>();
	// set.add("25");
	// q.goldenAnswers.put("en", set);
	// q.aggregation = true;
	// double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
	// double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
	// double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
	// log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
	// assertTrue(precision == 0 && recall == 0 && fMeasure == 0);
	// }
	//
	// @Test
	// public void selectTestResource() {
	// Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
	// systemAnswers.add(new
	// ResourceImpl("http://dbpedia.org/resource/Ghostface_Killah"));
	// systemAnswers.add(new
	// ResourceImpl("http://dbpedia.org/resource/Method_Man"));
	//
	// Question q = new Question();
	// q.pseudoSparqlQuery =
	// "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri  WHERE {         res:Wu-Tang_Clan dbo:bandMember ?uri .         ?uri text:\"stage name\" ?x .         ?x text:\"from\" text:\"movie\" . }";
	// HashSet<String> set = new HashSet<>();
	// set.add("http://dbpedia.org/resource/Ghostface_Killah");
	// set.add("http://dbpedia.org/resource/Method_Man");
	// q.goldenAnswers.put("en", set);
	// q.aggregation = true;
	// double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
	// double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
	// double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
	// log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
	// assertTrue(precision == 1 && recall == 1 && fMeasure == 1);
	// }
	//
	// @Test
	// public void selectTestResourceFalse() {
	// Set<RDFNode> systemAnswers = new HashSet<RDFNode>();
	// systemAnswers.add(new
	// ResourceImpl("http://dbpedia.org/resource/Ghostface_Killah"));
	// systemAnswers.add(new
	// ResourceImpl("http://dbpedia.org/resource/Barack Obama"));
	//
	// Question q = new Question();
	// q.pseudoSparqlQuery =
	// "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> SELECT DISTINCT ?uri  WHERE {         res:Wu-Tang_Clan dbo:bandMember ?uri .         ?uri text:\"stage name\" ?x .         ?x text:\"from\" text:\"movie\" . }";
	// HashSet<String> set = new HashSet<>();
	// set.add("http://dbpedia.org/resource/Ghostface_Killah");
	// set.add("http://dbpedia.org/resource/Method_Man");
	// q.goldenAnswers.put("en", set);
	// q.aggregation = true;
	// double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
	// double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
	// double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
	// log.info("P=" + precision + " R=" + recall + " F=" + fMeasure);
	// assertTrue(precision == 0.5 && recall == 0.5 && fMeasure == 0.5);
	// }
}
