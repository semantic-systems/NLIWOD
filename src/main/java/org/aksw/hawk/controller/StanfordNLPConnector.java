package org.aksw.hawk.controller;

import java.util.ArrayList;
import java.util.Collections;
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
	 * lemma,ner, parse, dcoref"
	 */
	public StanfordNLPConnector() {

		Properties props = new Properties();
		// props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner,
		// parse,mention, dcoref");
		props.setProperty("annotators", "tokenize, ssplit,pos,lemma, ner,parse");

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

	/**
	 * Copy Paste from hawk.nlp.ParseTree to make this class independent from
	 * Code which uses ClearNLP
	 * 
	 * @param sentence
	 * @param list
	 * @return
	 */
	private String replaceLabelsByIdentifiedURIs(String sentence, final List<Entity> list) {
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
		/**
		 * We aim at having only one node for named entities. (same with
		 * compoundNouns in recursive function) So we replace named entities
		 * with their url, wich is one coherent String. -> graph parser will
		 * give us only one node.
		 * 
		 */
		String sentence = this.replaceNamedEntitysWithURL(q);
		log.debug(sentence);

		Annotation document = this.runAnnotation(sentence);

		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		CoreMap sen = sentences.get(0);

		SemanticGraph graph = sen.get(CollapsedCCProcessedDependenciesAnnotation.class);

		MutableTree tree = semanticGraphToMutableTree(graph, q);

		log.debug(tree.toString());

		// used in main
		// out.append(q.getLanguageToQuestion().get("en") + "\r\n\r\n");
		// out.append(sen.get(CollapsedCCProcessedDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST)
		// + "\r\n\r\n");
		// out.append(graph.toString() + "\r\n\r\n");
		// out.append(tree.toString() + "\r\n\r\n");
		// out.append("\r\n\r\n\r\n\r\n");
		return tree;

	}

	/**
	 * Generates POS tags with StanfordCoreNLP. This has an equivalent in
	 * SentenceToSequence Maybe Keep this for testing?
	 * 
	 * @param document
	 * @return
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
	 * @param graph
	 * @return
	 */
	private MutableTree semanticGraphToMutableTree(final SemanticGraph graph, final HAWKQuestion q) {
		log.debug("use following tree for more than compound noun combination??");
		log.debug(graph.toString(SemanticGraph.OutputFormat.LIST));

		nodeNumber = 0;
		MutableTree tree = new MutableTree();
		MutableTreeNode mutableRoot;

		this.visitedNodes = new HashSet<>();
		IndexedWord graphRoot = graph.getFirstRoot();

		mutableRoot = new MutableTreeNode(graphRoot.word(), graphRoot.tag(), "root", null, nodeNumber++, graphRoot.lemma());
		tree.head = mutableRoot;

		convertGraphStanford(mutableRoot, graphRoot, graph, q);

		return tree;
	}

	/**
	 * The recursive Function to convert a Semantic Graph into MutableTree
	 * 
	 * @param parentMutableNode
	 * @param parentGraphWord
	 * @param graph
	 */
	private void convertGraphStanford(final MutableTreeNode parentMutableNode, final IndexedWord parentGraphWord, final SemanticGraph graph, final HAWKQuestion q) {
		visitedNodes.add(parentGraphWord);

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

	public static void main(final String[] args) {
		HAWKQuestion q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "One million and twenty four Eiffel Towers");
		StanfordNLPConnector stanford = new StanfordNLPConnector();
		stanford.combineSequences(q);

		long startTime = System.currentTimeMillis();
		stanford.combineSequences(q);
		long estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("execution speed: " + estimatedTime);

		q = new HAWKQuestion();
		q.getLanguageToQuestion().put("en", "twenty four thousand and nineteen hundred seventy four");
		stanford.combineSequences(q);
		startTime = System.currentTimeMillis();
		stanford.combineSequences(q);
		estimatedTime = System.currentTimeMillis() - startTime;
		System.out.println("execution speed: " + estimatedTime);

	}

	// StanfordNLPConnector stanford;
	// List<HAWKQuestion> questionsStanford;
	// Fox nerdModule = new Fox();
	// List<IQuestion> loadedQuestions =
	// QALD_Loader.load(Dataset.QALD6_Train_Multilingual);
	// questionsStanford = HAWKQuestionFactory.createInstances(loadedQuestions);
	// stanford = new StanfordNLPConnector();
	//
	// for (HAWKQuestion currentQuestion : questionsStanford) {
	// currentQuestion.setLanguageToNamedEntites(nerdModule.getEntities(currentQuestion.getLanguageToQuestion().get("en")));
	// // Annotation doc = stanford.runAnnotation(currentQuestion);
	// stanford.combineSequences(currentQuestion);
	// // stanford.combineSequences(doc, currentQuestion);
	//
	// }
	// try {
	//
	// File file = new File("stanford_compound.txt");
	//
	// file.createNewFile();
	//
	// FileWriter fw = new FileWriter(file.getAbsoluteFile());
	// BufferedWriter bw = new BufferedWriter(fw);
	// bw.write(out.toString());
	// bw.close();
	//
	// System.out.println("Done");
	//
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// }

}
