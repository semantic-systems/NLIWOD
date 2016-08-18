package org.aksw.hawk.experiment;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
public class Inzidenzmatrix {
	public static void main(String[] args) {

		List<HAWKQuestion> questions = null;
		questions = HAWKQuestionFactory.createInstances(QALD_Loader.load(Dataset.QALD6_Train_Hybrid));

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		for (Question q : questions) {
			//String text = q.getLanguageToQuestion().get("en");
			String text = "Who is the leader of germany?";
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
			// graph = sen.get(BasicDependenciesAnnotation.class);
			// System.out.println(graph);

			// tranform graph to incidence matrix M with M_ij = 1 if v_i is in e_j
			// just iterate over all edges e_j = (v_j1, v_j2) 
			List<SemanticGraphEdge> edges = graph.edgeListSorted();
			IndexedWord[] vertices = graph.vertexSet().stream().toArray(IndexedWord[]::new);
			// create incidence Matrix
			Integer[][] incidenceMatrix = new Integer[vertices.length][edges.size()];
			for(int i=0; i < vertices.length; i++){
				for(int j=0; j < edges.size(); j++){
					incidenceMatrix[i][j] = 0;
				}
			}
			// fill incidence matrix
			for(int i=0; i < vertices.length; i++){
				for(int j=0; j < edges.size(); j++){
					IndexedWord vertex = vertices[i];
					SemanticGraphEdge edge = edges.get(j);
					// Mij = 1 if v_i is in e_j, and our graph is undirected
					if(vertex.equals(edge.getSource()) || vertex.equals(edge.getTarget())){
						incidenceMatrix[i][j] = 1;
						}
					}
				}
			System.out.println(graph);
			for(int i=0; i<vertices.length; i++){
				System.out.println(Arrays.toString(incidenceMatrix[i]) + "\n");
			}
			break;
		}
	}
}
