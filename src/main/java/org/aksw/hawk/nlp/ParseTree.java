package org.aksw.hawk.nlp;

import java.io.IOException;
import java.util.List;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.autosparql.commons.qald.uri.Entity;
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

	public DEPTree process(Question q) {
		return process(tokenizer, components, q);
	}

	private DEPTree process(AbstractTokenizer tokenizer, AbstractComponent[] components, Question q) {
		String sentence = q.languageToQuestion.get("en");
		if (!q.languageToNamedEntites.isEmpty()) {
			sentence = replaceLabelsByIdentifiedURIs(sentence, q.languageToNamedEntites.get("en"));
			log.debug(sentence);
		}

		DEPTree tree = NLPGetter.toDEPTree(tokenizer.getTokens(sentence));

		for (AbstractComponent component : components)
			component.process(tree);

		log.debug(TreeTraversal.inorderTraversal(tree.getFirstRoot(), 0, null));
		return tree;
	}

	private String replaceLabelsByIdentifiedURIs(String sentence, List<Entity> list) {
		for (Entity entity : list) {
			if(!entity.label.equals("")){
			sentence = sentence.replace(entity.label, entity.uris.get(0).getURI());}
			else{
				log.error("Entity has no label in sentence: "+ sentence);
			}
		}
		return sentence;
	}

}