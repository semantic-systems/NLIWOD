package org.aksw.hawk.controller;

import java.util.List;

import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.MutableTreePruner;
import org.aksw.hawk.nouncombination.NounCombinationChain;
import org.aksw.hawk.nouncombination.NounCombiners;
import org.aksw.hawk.number.UnitController;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQueryBuilder;
import org.aksw.qa.annotation.spotter.ASpotter;
import org.aksw.qa.annotation.spotter.Spotlight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineStanford extends AbstractPipeline {
	static Logger log = LoggerFactory.getLogger(PipelineStanford.class);
	private ASpotter nerdModule;
	private MutableTreePruner pruner;
	private Annotater annotater;
	private SPARQLQueryBuilder queryBuilder;
	private Cardinality cardinality;
	private QueryTypeClassifier queryTypeClassifier;
	private StanfordNLPConnector stanfordConnector;
	private UnitController numberToDigit;
	private NounCombinationChain nounCombination;

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

		SPARQL sparql = new SPARQL();
		annotater = new Annotater(sparql);

		queryBuilder = new SPARQLQueryBuilder(sparql);
	}

	@Override
	public List<Answer> getAnswersToQuestion(final HAWKQuestion q) {
		log.info("Question: " + q.getLanguageToQuestion().get("en"));

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
		annotater.annotateTree(q);

		// Calculating all possible SPARQL BGPs with given semantic annotations
		log.info("Calculating SPARQL representations.");
		List<Answer> answers = queryBuilder.build(q);

		return answers;
	}

	public static void main(final String[] args) {
		PipelineStanford p = new PipelineStanford();
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "Which anti-apartheid activist was born in Mvezo?");
		p.getAnswersToQuestion(q);

	}

}
