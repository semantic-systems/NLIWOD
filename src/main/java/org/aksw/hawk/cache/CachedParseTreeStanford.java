/**
 * If Ricardo approves, this will be deprecated. Functionality moved to StanfordNLPConnector.
 */
//package org.aksw.hawk.cache;
//
//import java.util.List;
//import java.util.Set;
//
//import org.aksw.hawk.nlp.MutableTree;
//import org.aksw.hawk.nlp.MutableTreeNode;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
//import edu.stanford.nlp.ling.IndexedWord;
//import edu.stanford.nlp.pipeline.Annotation;
//import edu.stanford.nlp.semgraph.SemanticGraph;
//import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
//import edu.stanford.nlp.semgraph.SemanticGraphEdge;
//import edu.stanford.nlp.util.CoreMap;
//
//public class CachedParseTreeStanford {
//	public static Logger log = LoggerFactory.getLogger(CachedParseTreeStanford.class);
//	private int nodeNumber;
//
//	// private StanfordCoreNLP pipeline;
//
//	// public MutableTree process(HAWKQuestion q) {
//	// String sentence = q.getLanguageToQuestion().get("en");
//	//
//	// // create an empty Annotation just with the given text
//	// Annotation document = new
//	// Annotation(q.getLanguageToQuestion().get("en"));
//	//
//	// // run all Annotators on this text
//	// pipeline.annotate(document);
//	//
//	// List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//	// CoreMap sen = sentences.get(0);
//	//
//	// SemanticGraph graph =
//	// sen.get(CollapsedCCProcessedDependenciesAnnotation.class);
//	//
//	// MutableTree tree = semanticGraphToMutableTree(graph);
//	// log.debug(tree.toString());
//	//
//	// return tree;
//	//
//	// }
//
//	public MutableTree process(Annotation document) {
//
//		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//		CoreMap sen = sentences.get(0);
//
//		SemanticGraph graph = sen.get(CollapsedCCProcessedDependenciesAnnotation.class);
//
//		MutableTree tree = semanticGraphToMutableTree(graph);
//		log.debug(tree.toString());
//
//		return tree;
//
//	}
//
//	// public CachedParseTreeStanford() {
//	//
//	// // Complete Annotator list at
//	// // http://stanfordnlp.github.io/CoreNLP/annotators.html
//	// Properties props = new Properties();
//	// props.setProperty("annotators",
//	// "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
//	// pipeline = new StanfordCoreNLP(props);
//	//
//	// }
//
//	// public CachedParseTreeStanford(StanfordCoreNLP stanfordPipe) {
//	// this.pipeline = stanfordPipe;
//	// }
//
//	// public void test() {
//	// HAWKQuestion q = new HAWKQuestion();
//	// Map<String, String> languageToQuestion = new HashMap<String, String>();
//	// languageToQuestion.put("en",
//	// "Which anti-apartheid activist was born in Mvezo?");
//	// q.setLanguageToQuestion(languageToQuestion);
//	// process(q);
//	// // ClearNLP
//	// System.out.println("NOW ClearNLP");
//	// CachedParseTreeClearnlp tree = new CachedParseTreeClearnlp();
//	// tree.process(q);
//	//
//	// }
//
//	// public static void main(String[] args) {
//	// new CachedParseTreeStanford().test();
//	// }
//
//	private MutableTree semanticGraphToMutableTree(SemanticGraph graph) {
//		nodeNumber = 0;
//		MutableTree tree = new MutableTree();
//		MutableTreeNode mutableRoot;
//
//		IndexedWord graphRoot = graph.getFirstRoot();
//
//		mutableRoot = new MutableTreeNode(graphRoot.word(), graphRoot.tag(), "root", null, nodeNumber++, graphRoot.lemma());
//		tree.head = mutableRoot;
//
//		convertGraphStanford(mutableRoot, graphRoot, graph);
//
//		return tree;
//	}
//
//	private void convertGraphStanford(MutableTreeNode parentMutableNode, IndexedWord parentGraphWord, SemanticGraph graph) {
//
//		if (!graph.hasChildren(parentGraphWord)) {
//			return;
//		}
//
//		Set<IndexedWord> childrenWords = graph.getChildren(parentGraphWord);
//
//		for (IndexedWord child : childrenWords) {
//
//			SemanticGraphEdge edge = graph.getEdge(parentGraphWord, child);
//			String depLabel = edge.getRelation().getShortName();
//			MutableTreeNode childMutableNode = new MutableTreeNode(child.word(), child.tag(), depLabel, parentMutableNode, nodeNumber++, child.lemma());
//			parentMutableNode.addChild(childMutableNode);
//			convertGraphStanford(childMutableNode, child, graph);
//		}
//
//	}
//
// }
