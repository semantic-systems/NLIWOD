package org.aksw.hawk.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.hawk.number.UnitController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.IndexAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;

/**
 * This class is a Connector between HAWK and StanfordNLP Usage: Either pass a
 * List of Annotators to Constructor, {Complete Annotator list at
 * http://stanfordnlp.github.io/CoreNLP/annotators.html} or use empty
 * Constructor with default annotators. Then use runAnnotation() and pass
 * returned Annotoation-Object to combineSequences(Annotation,HAWKQuestion) to
 * find and registerNounPhrases in given Question or pass Annotation-Object to
 * process(Annotation) to get dependency-Graph
 *
 *
 * @author Jonathan
 */
public class StanfordNLPConnector {

	private StanfordCoreNLPClient stanfordPipe;
	private int nodeNumber;
	private Set<IndexedWord> visitedNodes;
	public static StringBuilder out = new StringBuilder();
	private static Logger log = LoggerFactory.getLogger(StanfordNLPConnector.class);
	private static final String STANFORD_IP = "139.18.2.39";
	private static final int STANFORD_PORT = 9000;
	private static final int USED_CORES = 4;

	/**
	 * Initializes the StanfordNLP with given Annotators.Complete Annotator list
	 * at http://stanfordnlp.github.io/CoreNLP/annotators.html}
	 *
	 * @param annotators Annotators to run
	 */
	public StanfordNLPConnector(final String annotators) {
		Properties props = new Properties();
		props.setProperty("annotators", annotators);
		stanfordPipe = new StanfordCoreNLPClient(props, STANFORD_IP, STANFORD_PORT, USED_CORES);

	}

	/**
	 * Initializes CoreNLP with default Annotators. "tokenize, ssplit, pos,
	 * lemma,ner, parse,"
	 */
	public StanfordNLPConnector() {

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos,lemma, ner,parse");

		stanfordPipe = new StanfordCoreNLPClient(props, STANFORD_IP, STANFORD_PORT, USED_CORES);
	}

	/**
	 * Runs StanfordNLP on given bare String and returns processed Annotation
	 *
	 * @see #preprocessStringForStanford(String)
	 *
	 * @param s The String to be processed.
	 * @return processed Question as Annotation
	 */
	public Annotation runAnnotation(final String s) {
		// create an empty Annotation just with the given text
		Annotation annotationDocument = new Annotation(preprocessStringForStanford(s));
		// run all Annotators on this text
		this.stanfordPipe.annotate(annotationDocument);
		return annotationDocument;
	}

	/**
	 * Runs StanfordNLP on given <Strong>HawkQuestion`s question in
	 * English</Strong> and returns processed Annotation
	 *
	 * @param q The HAWKQuestion to be processed.
	 * @return processed Question as Annotation
	 */
	public Annotation runAnnotation(final HAWKQuestion q) {
		// create an empty Annotation just with the given text
		String s = preprocessStringForStanford(q.getLanguageToQuestion().get("en"));
		Annotation annotationDocument = new Annotation(s);
		// run all Annotators on this text
		this.stanfordPipe.annotate(annotationDocument);
		return annotationDocument;
	}

	/**
	 * Extracts dependency Graph from processed Annotation.
	 *
	 * @param document an Processed Annotation
	 * @return Dependency Graph as MutableTree
	 */
	public MutableTree process(final Annotation document) {

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		CoreMap sen = sentences.get(0);

		SemanticGraph graph = sen.get(CollapsedCCProcessedDependenciesAnnotation.class);

		MutableTree tree = semanticGraphToMutableTree(graph, null);

		// log.debug(tree.toString());

		return tree;

	}

	/**
	 * Runs Stanford Pipeline on given question. Extracts dependency graph,
	 * POStags,lemma
	 *
	 * <pre>
	 * Note: Extracted MutableTree will be set in HAWKQuestion>
	 * </pre>
	 *
	 * Note:
	 *
	 * @param q Question to process.
	 * @return Extracted MutableTree.
	 */
	public MutableTree parseTree(final HAWKQuestion q, final UnitController numberToDigit) {
		String sentence = HAWKUtils.replaceNamedEntitysWithURL(q);
		log.info(sentence);
		if (numberToDigit != null) {
			sentence = numberToDigit.normalizeNumbers("en", sentence);
			log.info(sentence);
		}
		Annotation document = this.runAnnotation(preprocessStringForStanford(sentence));
		MutableTree tree = this.process(document);
		q.setTree(tree);
		return tree;
	}

	/**
	 * Generates POS tags with StanfordCoreNLP. This has an equivalent in
	 * SentenceToSequence Maybe Keep this for testing?
	 *
	 */
	public Map<String, String> generatePOSTags(final Annotation document) {

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		CoreMap sen = sentences.get(0);

		List<String> tokens = new LinkedList<>();
		Map<String, String> label2pos = new HashMap<>();

		for (CoreLabel token : sen.get(TokensAnnotation.class)) {
			String word = token.get(TextAnnotation.class);
			String pos = token.get(PartOfSpeechAnnotation.class);
			tokens.add(word);
			label2pos.put(word, pos);

		}

		return label2pos;

	}

	/**
	 * Converts a SemanticGraph from StanfordNLP to a Mutable tree recursively
	 * with DFS.
	 *
	 */
	private MutableTree semanticGraphToMutableTree(final SemanticGraph graph, final HAWKQuestion q) {
		log.debug(graph.toString());
		nodeNumber = 0;
		MutableTree tree = new MutableTree();
		MutableTreeNode mutableRoot;

		this.visitedNodes = new HashSet<>();
		IndexedWord graphRoot = graph.getFirstRoot();

		mutableRoot = new MutableTreeNode(postprocessStringForStanford(graphRoot), graphRoot.tag(), "root", null, nodeNumber++, graphRoot.lemma(), graphRoot.get(IndexAnnotation.class));
		tree.setRoot(mutableRoot);

		convertGraphStanford(mutableRoot, graphRoot, graph);

		return tree;
	}

	/**
	 * Stanford splits by sentences with "(" and ")" in them. But they occur on
	 * a regular basis through replacing names with url, because different
	 * meanings of a named entity are specified in brackets:
	 *
	 * <pre>
	 * http://dbpedia.org/page/Pound_(mass)
	 * http://dbpedia.org/page/Pound_(band)
	 * </pre>
	 *
	 * So we replace round brackets with "////"
	 *
	 * @see #postprocessStringForStanford(IndexedWord)
	 */
	public String preprocessStringForStanford(final String input) {
		try {
			return input.replaceAll("[()]", "////");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return input;
	}

	/**
	 * Deals with the problem of Stanford splitting by "(" and ")" in a previous
	 * step, we replace this by "////" This method regexes it back to normal.
	 *
	 * @see #preprocessStringForStanford(String)
	 */
	public String postprocessStringForStanford(final IndexedWord node) {
		return node.word().replaceAll("(////)(.+)(////)", "($2)");
	}

	/**
	 * The recursive Function to convert a Semantic Graph into MutableTree
	 *
	 */
	private void convertGraphStanford(final MutableTreeNode parentMutableNode, final IndexedWord parentGraphWord, final SemanticGraph graph) {
		visitedNodes.add(parentGraphWord);
		parentGraphWord.setWord(postprocessStringForStanford(parentGraphWord));
		/**
		 * If the parentNode is a named Entity , set POS tag to "ADD". At this
		 * point, only named entities are replaced by URLs
		 */
		if (parentGraphWord.word().contains("http://dbpedia.org/resource/")) {
			parentMutableNode.posTag = "ADD";
		}

		/**
		 * Remove already visited nodes from the list of children. This makes
		 * the graph for us acyclic and therefore prevents endless recursion.
		 */
		Set<IndexedWord> notCyclicChildren = new HashSet<>(graph.getChildren(parentGraphWord));
		notCyclicChildren.removeAll(visitedNodes);

		if (notCyclicChildren.isEmpty()) {
			return;
		}

		for (IndexedWord child : notCyclicChildren) {
			SemanticGraphEdge edge = graph.getEdge(parentGraphWord, child);
			String depLabel = edge.getRelation().getShortName();
			int wordPos = child.get(IndexAnnotation.class);

			MutableTreeNode childMutableNode = new MutableTreeNode(child.word(), child.tag(), depLabel, parentMutableNode, nodeNumber++, child.lemma(), wordPos);
			parentMutableNode.addChild(childMutableNode);
			convertGraphStanford(childMutableNode, child, graph);
		}

	}

	public static String getStanfordIp() {
		return STANFORD_IP;
	}

	public static int getStanfordPort() {
		return STANFORD_PORT;
	}

}
