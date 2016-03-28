package hawk;

import static org.junit.Assert.fail;

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

public class ParseTreeTest {

	@Test
	public void testProcess() {
		Logger log = LoggerFactory.getLogger(ParseTreeTest.class);
		String stanfordTree=new String();
		String clearNLPTree=new String();
		List<IQuestion> load = QALD_Loader.load(Dataset.QALD6_Train_Hybrid);
		List<HAWKQuestion> questions = HAWKQuestionFactory.createInstances(load);
		QueryTypeClassifier queryTypeClassifier = new QueryTypeClassifier();
		Fox nerdModule = new Fox();
		Cardinality cardinality = new Cardinality();
		CachedParseTreeStanford cParseTree = new CachedParseTreeStanford();
		CachedParseTreeClearnlp cParseTree2 = new CachedParseTreeClearnlp();
		for (HAWKQuestion q:questions){
			cParseTree.process(q);
			stanfordTree=cParseTree.toString();			
			//cParseTree2.process(q);
			//clearNLPTree=cParseTree2.toString();
		}
	}

}
