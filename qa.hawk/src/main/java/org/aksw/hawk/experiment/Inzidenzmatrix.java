package org.aksw.hawk.experiment;

import java.util.List;
import java.util.Properties;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class Inzidenzmatrix {
	public static void main(String[] args) {

		List<HAWKQuestion> questions = null;
		questions = HAWKQuestionFactory.createInstances(LoaderController.load(Dataset.QALD6_Train_Hybrid));

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize,ssplit,pos,lemma,depparse,parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		for (Question q : questions) {
			String text = q.getLanguageToQuestion().get("en");
			System.out.println("\n" + text);
			Annotation doc = new Annotation(text);
			pipeline.annotate(doc);
			List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
			CoreMap sen = sentences.get(0);
			// SemanticGraph graph =
			// sen.get(CollapsedDependenciesAnnotation.class);
			// System.out.println(graph);
			// NOTE ccprocessed and collapsed dependencies are not neccesarily a
			// DAG
			SemanticGraph graph = sen.get(CollapsedCCProcessedDependenciesAnnotation.class);
			System.out.println(graph);
			// graph = sen.get(BasicDependenciesAnnotation.class);
			// System.out.println(graph);

			// tranform to incidence matrix
			break;
		}
	}
}
