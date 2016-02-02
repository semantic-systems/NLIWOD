package org.aksw.hawk.controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.cache.CachedParseTree;
import org.aksw.hawk.nlp.MutableTreePruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pipeline {
	static Logger log = LoggerFactory.getLogger(Pipeline.class);
	private Fox nerdModule;
	private CachedParseTree cParseTree;
	private SentenceToSequence sentenceToSequence;
	private MutableTreePruner pruner;
	private Annotater annotater;
	private SPARQLQueryBuilder queryBuilder;
	private Cardinality cardinality;
	private QueryTypeClassifier queryTypeClassifier;

	public Pipeline() {
		queryTypeClassifier = new QueryTypeClassifier();

		nerdModule = new Fox();
		// controller.nerdModule = new Spotlight();
		// controller.nerdModule =new TagMe();
		// controller.nerdModule = new WikipediaMiner();
		// controller.nerdModule = new MultiSpotter(fox, tagMe, wiki, spot);

		cParseTree = new CachedParseTree();

		cardinality = new Cardinality();

		sentenceToSequence = new SentenceToSequence();

		pruner = new MutableTreePruner();

		SPARQL sparql = new SPARQL();
		annotater = new Annotater(sparql);

		queryBuilder = new SPARQLQueryBuilder(sparql);
	}

	public List<Answer> getAnswersToQuestion(Question q) {
		log.info("Question: " + q.languageToQuestion.get("en"));

		log.info("Classify question type.");
		q.isClassifiedAsASKQuery = queryTypeClassifier.isASKQuery(q.languageToQuestion.get("en"));

		// Disambiguate parts of the query
		log.info("Named entity recognition.");
		q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));

		// Noun combiner, decrease #nodes in the DEPTree
		log.info("Noun phrase combination.");
		// TODO make this method return the combine sequence and work on this, i.e., q.sequence = 	sentenceToSequence.combineSequences(q);
		sentenceToSequence.combineSequences(q);

		// Build trees from questions and cache them
		log.info("Dependency parsing.");
		q.tree = cParseTree.process(q);

		// Cardinality identifies the integer i used for LIMIT i
		log.info("Cardinality calculation.");
		q.cardinality = cardinality.cardinality(q);

		// Apply pruning rules
		log.info("Pruning tree.");
		q.tree = pruner.prune(q);

		// Annotate tree
		log.info("Semantically annotating the tree.");
		annotater.annotateTree(q);

		// Calculating all possible SPARQL BGPs with given semantic annotations
		log.info("Calculating SPARQL representations.");
		List<Answer> answers = queryBuilder.build(q);

		return answers;
	}

	private void write(Set<EvalObj> evals) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("results.html"));
			bw.write("<script src=\"sorttable.js\"></script><table class=\"sortable\">");
			bw.newLine();
			bw.write(" <tr>     <th>id</th><th>Question</th><th>F-measure</th><th>Precision</th><th>Recall</th><th>Comment</th>  </tr>");
			for (EvalObj eval : evals) {
				bw.write(" <tr>    <td>" + eval.getId() + "</td><td>" + eval.getQuestion() + "</td><td>" + eval.getFmax() + "</td><td>" + eval.getPmax() + "</td><td>" + eval.getRmax() + "</td><td>" + eval.getComment() + "</td>  </tr>");
				bw.newLine();
			}
			bw.write("</table>");
			bw.newLine();
			bw.close();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

}
