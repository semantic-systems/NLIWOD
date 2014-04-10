package org.aksw.hawk.module;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.autosparql.commons.qald.uri.Entity;
import org.aksw.hawk.index.DBAbstractsIndex;
import org.aksw.hawk.nlp.SentenceDetector;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.spotter.Spotlight;
import org.aksw.hawk.nlp.spotter.TagMe;
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;

public class SystemAnswerer {
	Logger log = LoggerFactory.getLogger(SystemAnswerer.class);
	private int sizeOfWindow = 5;
	private DBAbstractsIndex abstractsIndex = new DBAbstractsIndex();

	public Set<RDFNode> answer(ParameterizedSparqlString pseudoQuery) {
		// for each full text part of the query ask abstract index
		List<Element> elements = ((ElementGroup) pseudoQuery.asQuery().getQueryPattern()).getElements();

		for (Element elem : elements) {
			if (elem instanceof ElementPathBlock) {
				ElementPathBlock pathBlock = (ElementPathBlock) elem;
				for (TriplePath triple : pathBlock.getPattern().getList()) {
					Node predicate = triple.getPredicate();
					// if predicate needs to be replaced
					// TODO work on cases
					if (!predicate.toString(false).startsWith("http:")) {
						String localName = predicate.getLocalName();

						// case 1: subject bound
						if (triple.getSubject().isConcrete()) {
							log.error("Cannot resolve hybrid query");
						}
						// case 2: object bound
						else if (triple.getObject().isConcrete()) {
							if (triple.getObject().isURI()) {
								List<Document> list = abstractsIndex.askForPredicateWithBoundAbstract(localName, triple.getObject().getURI());
								for (Document doc : list) {
									List<String> ne = extractPossibleNEFromDoc(doc, localName, triple.getObject().getURI());
									log.debug("\t" + Joiner.on("\n").join(ne));
								}

							} else {
								log.error("Cannot resolve hybrid query");

							}
						}
						// case 3: both are bound
						else if (triple.getObject().isConcrete() && triple.getSubject().isConcrete()) {
							log.error("Cannot resolve hybrid query");
						}
						// case 4: neither subject nor object are bound
						else {
							log.error("Cannot resolve hybrid query");
						}
					}
					// if object needs to be replaced
				}
			}
		}

		return null;
	}

	private List<String> extractPossibleNEFromDoc(Document doc, String surrounding, String alreadyIdentifiedNE) {
		String text = doc.get(DBAbstractsIndex.FIELD_NAME_OBJECT);
		// detect sentences
		SentenceDetector sd = new SentenceDetector();
		String[] sentences = sd.detectSentences(text);
		// detect sentence via surrounding word
		ArrayList<String> window = new ArrayList<>();
		for (int i = 0; i < sentences.length; ++i) {
			if (sentences[i].contains(surrounding)) {
				for (int j = i - sizeOfWindow / 2; j < i + sizeOfWindow / 2.0; ++j) {
					if (!(j < 0) && !(j > sentences.length)) {
						window.add(sentences[j]);
					}
				}
			}
		}
		String windowText = Joiner.on("\n").join(window);
		log.debug(windowText);
		// extract possible Named Entities (NE) via NERD modules
		ASpotter tagger = new Spotlight();
		Map<String, List<Entity>> nes = tagger.getEntities(windowText);
		for (String key : nes.keySet()) {
			System.out.println(key);
			for (Entity entity : nes.get(key)) {
				System.out.println("\t" + entity.label + " ->" + entity.type);
				for (Resource r : entity.posTypesAndCategories) {
					System.out.println("\t\tpos: " + r);
				}
				for (Resource r : entity.uris) {
					System.out.println("\t\turi: " + r);
				}
			}
		}
		return Lists.newArrayList();
	}
}
