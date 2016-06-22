package org.aksw.hawk.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.aksw.hawk.datastructures.HAWKQuestion;
import org.aksw.hawk.datastructures.HAWKQuestionFactory;
import org.aksw.hawk.nlp.MutableTree;
import org.aksw.hawk.nlp.MutableTreeNode;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.number.UnitController;
import org.aksw.hawk.spotter.Spotlight;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import edu.stanford.nlp.international.Language;
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
import edu.stanford.nlp.trees.GrammaticalRelation;
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
	public static StringBuilder out = new StringBuilder();
	static Logger log = LoggerFactory.getLogger(StanfordNLPConnector.class);

	/**
	 * Initializes the StanfordNLP with given Annotators.Complete Annotator list
	 * at http://stanfordnlp.github.io/CoreNLP/annotators.html}
	 * 
	 * @param annotators Annotators to run
	 */
	public StanfordNLPConnector(final String annotators) {
		Properties props = new Properties();
		props.setProperty("annotators", annotators);
		stanfordPipe = new StanfordCoreNLP(props);

	}

	/**
	 * Initializes CoreNLP with default Annotators. "tokenize, ssplit, pos,
	 * lemma,ner, parse,"
	 */
	public StanfordNLPConnector() {

		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit,pos,lemma, ner,parse");

		stanfordPipe = new StanfordCoreNLP(props);
	}

	/**
	 * Copy Paste from hawk.nlp.ParseTree to make this class independent from
	 * Code which uses ClearNLP
	 * 
	 */

	// TODO @ christian einen test schreiben mit der frage : Who was vice
	// president under the president who approved the use of atomic weapons
	// against Japan during World War II?
	private String replaceNamedEntitysWithURL(final HAWKQuestion q) {
		String sentence = q.getLanguageToQuestion().get("en");
		if (!q.getLanguageToNamedEntites().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNamedEntites().get("en"));
			log.debug(sentence);
		}
		if (!q.getLanguageToNounPhrases().isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.getLanguageToNounPhrases().get("en"));
			log.debug(sentence);
		}

		return sentence;
	}

	// TODO replace this code change also in clearnlp by introducing one util
	// class
	/**
	 *
	 * Copy Paste from hawk.nlp.ParseTree to make this class independent from
	 * Code which uses ClearNLP
	 * 
	 */

	private String replaceLabelsByIdentifiedURIs(final String sentence, final List<Entity> list) {
		/*
		 * reverse list of entities to start replacing from the end of the
		 * string so that replacing from the end won't mess up the order
		 */
		List<String> textParts = new ArrayList<>();

		list.sort(Comparator.comparing(Entity::getOffset).reversed());
		int startFormerLabel = sentence.length();
		for (Entity currentNE : list) {
			// proof if this label undercuts the last one.
			int currentNEStartPos = currentNE.getOffset();
			int currentNEEndPos = currentNEStartPos + currentNE.label.length();
			if (startFormerLabel >= currentNEEndPos) {
				textParts.add(sentence.substring(currentNEEndPos, startFormerLabel));
				textParts.add(currentNE.uris.get(0).getURI());
				startFormerLabel = currentNEStartPos;
			}
		}
		if (startFormerLabel > 0) {
			textParts.add(sentence.substring(0, startFormerLabel));
		}
		StringBuilder textWithMarkups = new StringBuilder();
		for (int i = textParts.size() - 1; i >= 0; --i) {
			textWithMarkups.append(textParts.get(i));
		}
		return textWithMarkups.toString();
	}

	/**
	 * Runs StanfordNLP on given Question and returns processed Annotation
	 * 
	 * @param q The HAWKQuestion to be processed.
	 * @return processed Question as Annotation
	 */
	public Annotation runAnnotation(final HAWKQuestion q) {
		// create an empty Annotation just with the given text
		Annotation annotationDocument = new Annotation(q.getLanguageToQuestion().get("en"));
		// run all Annotators on this text
		this.stanfordPipe.annotate(annotationDocument);
		return annotationDocument;
	}

	/**
	 * Runs StanfordNLP on given String and returns processed Annotation
	 * 
	 * @param q The HAWKQuestion to be processed.
	 * @return processed Question as Annotation
	 */
	public Annotation runAnnotation(final String s) {
		// create an empty Annotation just with the given text
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

		log.debug(tree.toString());

		return tree;

	}

	/**
	 * Runs a NounPhrasCombination and dependency parsing on given HAWKQuestion
	 * 
	 * @param q The Question you want to run NounPhraseCombination on
	 */
	public MutableTree combineSequences(final HAWKQuestion q) {
		return this.combineSequences(q, null);
	}

	/**
	 * Runs a NounPhrasCombination and dependency parsing on given HAWKQuestion
	 * 
	 * @param q The Question you want to run NounPhraseCombination on
	 */
	public MutableTree combineSequences(final HAWKQuestion q, final UnitController numberToDigit) {
		/**
		 * We aim at having only one node for named entities. (same with
		 * compoundNouns in recursive function) So we replace named entities
		 * with their url, which is one coherent String. -> graph parser will
		 * give us only one node.
		 * 
		 */
		String sentence = this.replaceNamedEntitysWithURL(q);
		log.info(sentence);
		if (numberToDigit != null) {
			sentence = numberToDigit.normalizeNumbers("en", sentence);
			log.info(sentence);
		}

		Annotation document = this.runAnnotation(preprocessStringForStanford(sentence));

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		CoreMap sen = sentences.get(0);

		SemanticGraph graph = sen.get(CollapsedCCProcessedDependenciesAnnotation.class);

		MutableTree tree = semanticGraphToMutableTree(graph, q);

		log.debug(tree.toString());

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
		log.debug("use following tree for more than compound noun combination??");
		log.debug(graph.toString(SemanticGraph.OutputFormat.LIST));
		nodeNumber = 0;
		MutableTree tree = new MutableTree();
		MutableTreeNode mutableRoot;

		this.visitedNodes = new HashSet<>();
		IndexedWord graphRoot = graph.getFirstRoot();

		mutableRoot = new MutableTreeNode(postprocessStringForStanford(graphRoot), graphRoot.tag(), "root", null, nodeNumber++, graphRoot.lemma());
		tree.head = mutableRoot;

		convertGraphStanford(mutableRoot, graphRoot, graph, q);

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
		return input.replaceAll("[()]", "////");
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
	private void convertGraphStanford(final MutableTreeNode parentMutableNode, final IndexedWord parentGraphWord, final SemanticGraph graph, final HAWKQuestion q) {
		visitedNodes.add(parentGraphWord);
		/**
		 * 
		 */
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

		/**
		 * Get all children which are in relation "compound" with parent
		 */
		if (q != null && !parentMutableNode.posTag.equals("ADD")) {
			GrammaticalRelation gr = new GrammaticalRelation(Language.UniversalEnglish, "compound", null, null);
			ArrayList<IndexedWord> compounds = new ArrayList<>();
			compounds.addAll(graph.getChildrenWithReln(parentGraphWord, gr));

			/**
			 * Don't combine with children which are URL from FOX (NER)
			 * 
			 */
			ArrayList<IndexedWord> removeMe = new ArrayList<>();
			for (IndexedWord child : compounds)
				if (child.word().contains("http://dbpedia.org/resource/")) {
					removeMe.add(child);
				}
			compounds.removeAll(removeMe);

			if (!compounds.isEmpty()) {

				/**
				 * We Need to order all Parts of the CompoundNoun for running
				 * SentenceToSequence.transformTree. ParentNode is also a part
				 * of the CompoundNoun
				 */
				compounds.add(parentGraphWord);
				Collections.sort(compounds);
				ArrayList<String> orderlyWords = new ArrayList<>();
				for (IndexedWord compoundChild : compounds) {
					orderlyWords.add(compoundChild.word());
				}
				/**
				 * This sets the CombinedNN as Entity in Question
				 */
				SentenceToSequence.transformTree(orderlyWords, q);
				/**
				 * Merge all compound-children with parent
				 */
				parentMutableNode.setPosTag("CombinedNN");
				parentMutableNode.setLabel(Joiner.on(" ").join(orderlyWords));
				parentMutableNode.lemma = "#url#";

				/**
				 * Get the children of eliminated Nodes and add then to children
				 * of parents e.g. deal with the children of the children of
				 * Pare
				 */

				compounds.remove(parentGraphWord);
				for (IndexedWord compoundChild : compounds) {
					notCyclicChildren.addAll(graph.getChildList(compoundChild));
				}

				notCyclicChildren.removeAll(compounds);
			} // end if CombinedNN
		} // end if HAWKQuestion !=null
		for (IndexedWord child : notCyclicChildren) {

			SemanticGraphEdge edge = graph.getEdge(parentGraphWord, child);
			String depLabel = edge.getRelation().getShortName();
			MutableTreeNode childMutableNode = new MutableTreeNode(child.word(), child.tag(), depLabel, parentMutableNode, nodeNumber++, child.lemma());
			parentMutableNode.addChild(childMutableNode);
			convertGraphStanford(childMutableNode, child, graph, q);
		}

	}

	// TODO transform to unit tests (should be three or more)
	public static void main(final String[] args) {
		StanfordNLPConnector connector = new StanfordNLPConnector();
		String sentence = "Who was vice president under the president who approved the use of atomic weapons against Japan during World War II?";
		List<Entity> list = Lists.newArrayList();
		Entity e = new Entity("vice president", "");
		e.uris.add(new ResourceImpl("http://dbpedia.org/resource/Vice_president"));
		e.setOffset(8);
		list.add(e);
		e = new Entity("president", "");
		e.uris.add(new ResourceImpl("http://dbpedia.org/resource/President"));
		e.setOffset(33);
		list.add(e);
		String string = connector.replaceLabelsByIdentifiedURIs(sentence, list);
		log.debug(string);
		Assert.assertEquals(string,
		        "Who was http://dbpedia.org/resource/Vice_president under the http://dbpedia.org/resource/President who approved the use of atomic weapons against Japan during World War II?");

		/// ---------------
		List<HAWKQuestion> questionsStanford;
		Spotlight nerdModule = new Spotlight();
		List<IQuestion> loadedQuestions = QALD_Loader.load(Dataset.QALD6_Train_Hybrid);
		questionsStanford = HAWKQuestionFactory.createInstances(loadedQuestions);

		for (HAWKQuestion currentQuestion : questionsStanford) {
			log.info(currentQuestion.getLanguageToQuestion().get("en"));
			currentQuestion.setLanguageToNamedEntites(nerdModule.getEntities(currentQuestion.getLanguageToQuestion().get("en")));
			// Annotation doc = stanford.runAnnotation(currentQuestion);
			connector.combineSequences(currentQuestion);
			// stanford.combineSequences(doc, currentQuestion);

		}

	}
}
