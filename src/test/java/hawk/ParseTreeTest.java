package hawk;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;

import org.aksw.hawk.cache.CachedParseTreeStanford;
import org.aksw.hawk.cache.CachedParseTreeClearnlp;
//import org.aksw.hawk.cache.CachedParseTree;
import org.aksw.hawk.controller.Cardinality;
import org.aksw.hawk.controller.Pipeline;
import org.aksw.hawk.controller.QueryTypeClassifier;
import org.aksw.hawk.datastructures.Answer;
import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.nlp.MutableTreePruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.spotter.Fox;
import org.aksw.hawk.querybuilding.Annotater;
import org.aksw.hawk.querybuilding.SPARQL;
import org.aksw.hawk.querybuilding.SPARQLQueryBuilder;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;

public class ParseTreeTest {

	@Test
	public void testProcess() throws IOException {
		Logger log = LoggerFactory.getLogger(ParseTreeTest.class);
		String stanfordTree=new String();
		String clearNLPTree=new String();
		Fox nerdModule = new Fox();


		Cardinality cardinality = new Cardinality();

		SentenceToSequence sentenceToSequence = new SentenceToSequence();

		MutableTreePruner pruner = new MutableTreePruner();

		SPARQL sparql = new SPARQL();
		Annotater annotater = new Annotater(sparql);

		List<HAWKQuestion> questions = null;

		questions = HAWKQuestionFactory.createInstances(QALD_Loader.load(Dataset.QALD6_Train_Hybrid));

		QueryTypeClassifier queryTypeClassifier = new QueryTypeClassifier();

		CachedParseTreeStanford cParseTree = new CachedParseTreeStanford();
		CachedParseTreeClearnlp cParseTree2 = new CachedParseTreeClearnlp();


		BufferedWriter bw = new BufferedWriter(new FileWriter("arboretum_stanford.txt", true));

		for (HAWKQuestion q:questions){
			//log.info("Classify question type.");
			//q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));

			// Disambiguate parts of the query
			//log.info("Named entity recognition.");
			//q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));
			//sentenceToSequence.combineSequences(q);

			// Build trees from questions and cache them
			log.info("Dependency parsing.");
			q.setTree(cParseTree.process(q));

			// Cardinality identifies the integer i used for LIMIT i
			//log.info("Cardinality calculation.");
			//q.setCardinality(cardinality.cardinality(q));

			// Apply pruning rules
			//log.info("Pruning tree.");
			//q.setTree(pruner.prune(q));

			// Annotate tree
			//log.info("Semantically annotating the tree.");
			//annotater.annotateTree(q);

			//cParseTree.process(q);
			try {

				bw.write(q.getLanguageToQuestion().toString());   
				bw.newLine();
				bw.write(q.getTree().toString());
				bw.newLine();
				bw.newLine();
				bw.flush();

			} 
			catch (IOException ioe) {
				ioe.printStackTrace();
			} 
			

		}
		if (bw != null) try {
			bw.close();
		} catch (IOException ioe2) {
			
		}
		BufferedWriter bw2 = new BufferedWriter(new FileWriter("arboretum_clearnlp.txt", true));
		questions = HAWKQuestionFactory.createInstances(QALD_Loader.load(Dataset.QALD6_Train_Hybrid));
		for (HAWKQuestion q:questions){
			//log.info("Classify question type.");
			//q.setIsClassifiedAsASKQuery(queryTypeClassifier.isASKQuery(q.getLanguageToQuestion().get("en")));
			// Disambiguate parts of the query
			//log.info("Named entity recognition.");
			//q.setLanguageToNamedEntites(nerdModule.getEntities(q.getLanguageToQuestion().get("en")));
			//sentenceToSequence.combineSequences(q);
			// Build trees from questions and cache them
			log.info("Dependency parsing.");
			q.setTree(cParseTree2.process(q));

			// Cardinality identifies the integer i used for LIMIT i
			//log.info("Cardinality calculation.");
			//q.setCardinality(cardinality.cardinality(q));

			// Apply pruning rules
			//log.info("Pruning tree.");
			//q.setTree(pruner.prune(q));

			// Annotate tree
			//log.info("Semantically annotating the tree.");
			//annotater.annotateTree(q);

			//cParseTree.process(q);
			try {

				bw2.write(q.getLanguageToQuestion().toString());   
				bw2.newLine();
				bw2.write(q.getTree().toString());
				bw2.newLine();
				bw2.newLine();
				bw2.flush();

			} 
			catch (IOException ioe) {
				ioe.printStackTrace();
			} 
			// always close the file

		}
		if (bw2 != null) try {
			bw2.close();
		} catch (IOException ioe2) {
			// just ignore it
		}
		
	}

}
