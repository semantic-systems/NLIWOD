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
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.qa.commons.datastructure.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
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

	private StanfordCoreNLP stanfordPipe;
	private int nodeNumber;
	private Set<IndexedWord> visitedNodes;
	static Logger log = LoggerFactory.getLogger(StanfordNLPConnector.class);

	/**
	 * Initializes the StanfordNLP with given Annotators.Complete Annotator list
	 * at http://stanfordnlp.github.io/CoreNLP/annotators.html}
	 * 
	 * @param annotators
	 *            Annotators to run
	 */
	public StanfordNLPConnector(String annotators) {
		Properties props = new Properties();
		props.setProperty("annotators", annotators);
		stanfordPipe = new StanfordCoreNLP(props);

	}

	/**
	 * Initializes CoreNLP with default Annotators.
	 * "tokenize, ssplit, pos, lemma,ner, parse, dcoref"
	 */
	public StanfordNLPConnector() {

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma,ner, parse, dcoref");
		stanfordPipe = new StanfordCoreNLP(props);

	}

	/**
	 * Copy Paste from hawk.nlp.ParseTree to make this class independent from
	 * Code which uses ClearNLP
	 * 
	 * @param sentence
	 * @param list
	 * @return
	 */

	private void replaceWithURI(HAWKQuestion q) {
		String sentence = q.getLanguageToQuestion().get("en");
		if (!q.getLanguageToNamedEntites().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNamedEntites().get("en"));
			log.debug(sentence);
		}
		if (!q.getLanguageToNounPhrases().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNounPhrases().get("en"));
			log.debug(sentence);
		}
	}

	/**
	 * Copy Paste from hawk.nlp.ParseTree to make this class independent from
	 * Code which uses ClearNLP
	 * 
	 * @param sentence
	 * @param list
	 * @return
	 */
	private String replaceLabelsByIdentifiedURIs(String sentence, List<Entity> list) {
		for (Entity entity : list) {
			if (!entity.label.equals("")) {
				// " " inserted so punctuation gets separated correctly from
				// URIs
				sentence = sentence.replace(entity.label, entity.uris.get(0).getURI() + " ").trim();
			} else {
				log.error("Entity has no label in sentence: " + sentence);
			}
		}
		return sentence;
	}

	/**
	 * Runs StanfordNLP on given Question and returns precessed Annotation
	 * 
	 * @param q
	 *            The HAWKQuestion to be processed.
	 * @return processed Question as Annotation
	 */
	public Annotation runAnnotation(HAWKQuestion q) {
		this.replaceWithURI(q);
		// create an empty Annotation just with the given text
		Annotation annotationDocument = new Annotation(q.getLanguageToQuestion().get("en"));
		// run all Annotators on this text
		this.stanfordPipe.annotate(annotationDocument);
		return annotationDocument;
	}

	/**
	 * Extracts dependency Graph from processed Annotation. Shoud have same
	 * return as CachedParseTreeClearnlp.process(HAWKQuestion)
	 * 
	 * @param document
	 *            an Processed Annotation
	 * @return Dependency Graph as MutableTree
	 */
	public MutableTree process(Annotation document) {

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		CoreMap sen = sentences.get(0);

		SemanticGraph graph = sen.get(CollapsedCCProcessedDependenciesAnnotation.class);

		MutableTree tree = semanticGraphToMutableTree(graph);
		log.debug(tree.toString());

		return tree;

	}

	/**
	 * Runs a NounPhrasCombination on given Annotation and HAWKQuestion. Given
	 * Annotation has to be the processed Annotation of given HAWKQuestion
	 * 
	 * @param document
	 *            processed Annotation of HAWKQuestion
	 * @param q
	 *            The Question you want to run NounPhraseCombination on
	 */
	public void combineSequences(Annotation document, HAWKQuestion q) {

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		CoreMap sen = sentences.get(0);
		List<String> tokens = new LinkedList<String>();
		Map<String, String> label2pos = new HashMap<String, String>();

		for (CoreLabel token : sen.get(TokensAnnotation.class)) {
			String word = token.get(TextAnnotation.class);
			String pos = token.get(PartOfSpeechAnnotation.class);
			tokens.add(word);
			label2pos.put(word, pos);
		}

		SentenceToSequence.runPhraseCombination(q, tokens, label2pos);

	}

	/**
	 * Generates POS tags with StanfordCoreNLP. This has an equivalent in
	 * SentenceToSequence Maybe Keep this for testing?
	 * 
	 * @param document
	 * @return
	 */
	public Map<String, String> generatePOSTags(Annotation document) {

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		CoreMap sen = sentences.get(0);
		List<String> tokens = new LinkedList<String>();
		Map<String, String> label2pos = new HashMap<String, String>();

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
	 * with DFS. The SemanticGraph CAN be cyclic, this is NOT catched yet.
	 * 
	 * @param graph
	 * @return
	 */
	private MutableTree semanticGraphToMutableTree(SemanticGraph graph) {
		nodeNumber = 0;
		MutableTree tree = new MutableTree();
		MutableTreeNode mutableRoot;

		this.visitedNodes = new HashSet<IndexedWord>();
		IndexedWord graphRoot = graph.getFirstRoot();

		mutableRoot = new MutableTreeNode(graphRoot.word(), graphRoot.tag(), "root", null, nodeNumber++, graphRoot.lemma());
		tree.head = mutableRoot;

		convertGraphStanford(mutableRoot, graphRoot, graph);

		return tree;
	}

	/**
	 * The recursive Function to convert a Semantic Graph into MutableTree
	 * 
	 * @param parentMutableNode
	 * @param parentGraphWord
	 * @param graph
	 */
	private void convertGraphStanford(MutableTreeNode parentMutableNode, IndexedWord parentGraphWord, SemanticGraph graph) {
		visitedNodes.add(parentGraphWord);

		// if graph is cyclic, it will be catched here
		Set<IndexedWord> notCyclicChildren = new HashSet<IndexedWord>(graph.getChildren(parentGraphWord));
		notCyclicChildren.removeAll(visitedNodes);

		if (notCyclicChildren.isEmpty()) {
			return;
		}

		for (IndexedWord child : notCyclicChildren) {

			SemanticGraphEdge edge = graph.getEdge(parentGraphWord, child);
			String depLabel = edge.getRelation().getShortName();
			MutableTreeNode childMutableNode = new MutableTreeNode(child.word(), child.tag(), depLabel, parentMutableNode, nodeNumber++, child.lemma());
			parentMutableNode.addChild(childMutableNode);
			convertGraphStanford(childMutableNode, child, graph);
		}

	}

}
