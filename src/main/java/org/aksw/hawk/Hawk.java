/**
 * 
 */
package org.aksw.hawk;

import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.cache.CachedParseTree;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.controller.TrainPipeline;
import org.aksw.hawk.nlp.MutableTreePruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQuery;
import org.aksw.hawk.querybuilding.SPARQLQueryBuilder;

import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * @author Lorenz Buehmann
 * 
 */
public class Hawk {

	public static void main(String[] args) throws Exception {
		TrainPipeline controller = new TrainPipeline();

		controller.nerdModule = new Fox();
		controller.cParseTree = new CachedParseTree();

		controller.sentenceToSequence = new SentenceToSequence();

		controller.annotater = new Annotater();

		SPARQL sparql = new SPARQL();
		controller.queryBuilder = new SPARQLQueryBuilder(sparql);

		controller.pruner = new MutableTreePruner();

		Question q = new Question();
		q.languageToQuestion.put("en", "Which recipients of the Victoria Cross died in the Battle of Arnhem?");

		Map<String, Answer> sparqll = controller.calculateSPARQLRepresentation(q);

		System.out.println(sparqll);
	}

}
