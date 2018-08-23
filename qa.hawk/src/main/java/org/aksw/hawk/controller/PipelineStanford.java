package org.aksw.hawk.controller;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.Annotater;
import org.aksw.hawk.nlp.Cardinality;
import org.aksw.hawk.nlp.MutableTreePruner;
import org.aksw.hawk.nouncombination.NounCombinationChain;
import org.aksw.hawk.nouncombination.NounCombiners;
import org.aksw.hawk.number.UnitController;
import org.aksw.hawk.querybuilding.PatternSparqlGenerator;
import org.aksw.hawk.util.PropertiesLoader;
import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Fox;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.aksw.qa.commons.sparql.SPARQL;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

public class PipelineStanford extends AbstractPipeline {
	static Logger log = LoggerFactory.getLogger(PipelineStanford.class);
	private ASpotter nerdModule;
	private MutableTreePruner pruner;
	private Annotater annotater;
	private PatternSparqlGenerator patternsparqlgenerator;
	private Cardinality cardinality;
	private QueryTypeClassifier queryTypeClassifier;
	private StanfordNLPConnector stanfordConnector;

	private UnitController numberToDigit;
	private NounCombinationChain nounCombination;

	private final Properties environment = PropertiesLoader.loadProperties();

	public PipelineStanford() {
		queryTypeClassifier = new QueryTypeClassifier();

		nerdModule = new Spotlight();
		// controller.nerdModule = new Spotlight();
		// controller.nerdModule =new TagMe();
		// controller.nerdModule = new MultiSpotter(fox, tagMe, wiki, spot);

		this.stanfordConnector = new StanfordNLPConnector();
		this.numberToDigit = new UnitController();

		numberToDigit.instantiateEnglish(stanfordConnector);
		nounCombination = new NounCombinationChain(NounCombiners.HawkRules, NounCombiners.StanfordDependecy);

		cardinality = new Cardinality();

		pruner = new MutableTreePruner();

		SPARQL sparql = new SPARQL(String.format("http://%s:%s/ds/sparql",
				environment.getProperty("fuseki.sparql.endpoint.url"),
				environment.getProperty("fuseki.sparql.endpoint.port")));
		annotater = new Annotater(sparql);

		patternsparqlgenerator = new PatternSparqlGenerator();
	}

	@Override
	public List<Answer> getAnswersToQuestion(final HAWKQuestion q) throws ExecutionException, RuntimeException, ParseException{
		log.info("Question: " + q.getLanguageToQuestion().get("en"));
		
	    q.setTransformedQuestion(q.getLanguageToQuestion().get("en"));

		log.info("Classify question type.");
		q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));
		

		// Disambiguate parts of the query
		log.info("Named entity recognition.");
		q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));
		
		// Noun combiner, decrease #nodes in the DEPTree
		log.info("Noun phrase combination / Dependency Parsing");
		// TODO make tlhis method return the combine sequence and work on this,
		// i.e., q.sequence = sentenceToSequence.combineSequences(q);

		// @Ricardo this will calculate cardinality of reduced(combinedNN) tree.
		// is this right?
		q.setTree(stanfordConnector.parseTree(q, this.numberToDigit));

		nounCombination.runChain(q);
		

		// Cardinality identifies the integer i used for LIMIT i
		log.info("Cardinality calculation.");
		q.setCardinality(cardinality.cardinality(q));

		// Apply pruning rules
		log.info("Pruning tree.");
		q.setTree(pruner.prune(q));
		

		// Annotate tree
		log.info("Semantically annotating the tree.");
		//annotater.annotateTree(q);

		// Calculating all possible SPARQL BGPs with given semantic annotations
		log.info("Calculating SPARQL representations.");
		List<Answer> answers = patternsparqlgenerator.build(q);
		

		return answers;
	}

	public StanfordNLPConnector getStanfordConnector() {
		return stanfordConnector;
	}

	public static void main(final String[] args) throws ExecutionException, RuntimeException, ParseException{
		PipelineStanford p = new PipelineStanford();
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "What is the capital of Germany?");
		p.getAnswersToQuestion(q);
		 
		p = new PipelineStanford();
		q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "Is horse racing a sport?");
		p.getAnswersToQuestion(q);

	}

}
