package org.aksw.hawk.nlp;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clearnlp.component.AbstractComponent;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.nlp.NLPGetter;
import com.clearnlp.nlp.NLPMode;
import com.clearnlp.reader.AbstractReader;
import com.clearnlp.tokenization.AbstractTokenizer;

/**
 * generates a dependency tree called predicate argument tree
 * 
 * @author r.usbeck
 * 
 */
public class ParseTree {
	Logger log = LoggerFactory.getLogger(getClass());

	final String language = AbstractReader.LANG_EN;
	final String modelType = "general-en";

	private AbstractComponent[] components;
	private AbstractTokenizer tokenizer;

	public ParseTree() {
		try {
			this.tokenizer = NLPGetter.getTokenizer(language);
			AbstractComponent tagger = NLPGetter.getComponent(modelType, language, NLPMode.MODE_POS);
			AbstractComponent parser = NLPGetter.getComponent(modelType, language, NLPMode.MODE_DEP);
			AbstractComponent identifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_PRED);
			AbstractComponent classifier = NLPGetter.getComponent(modelType, language, NLPMode.MODE_ROLE);
			AbstractComponent labeler = NLPGetter.getComponent(modelType, language, NLPMode.MODE_SRL);

			this.components = new AbstractComponent[] { tagger, parser, identifier, classifier, labeler };
		} catch (IOException e) {
			log.error("IO Error while initializing ParseTree", e);
		}
	}

	public DEPTree process(String input) {
		return process(tokenizer, components, input);
	}

	private DEPTree process(AbstractTokenizer tokenizer, AbstractComponent[] components, String sentence) {
		DEPTree tree = NLPGetter.toDEPTree(tokenizer.getTokens(sentence));

		for (AbstractComponent component : components)
			component.process(tree);

		log.debug(TreeTraversal.inorderTraversal(tree.getFirstRoot(), 0, null));
		return tree;
	}

	public static void main(String[] args) {

		ParseTree parseTree = new ParseTree();
		DEPTree tree = parseTree.process("Give me all currencies of G8 countries.");
		 
		tree = parseTree.process("Give me all http://dbpedia.org/resource/Currency of http://dbpedia.org/resource/G8.");
	}
}