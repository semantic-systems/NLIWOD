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
import org.apache.lucene.document.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
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
					// only use full-text lookup is triple has full-text part
					if (triple.getPredicate().getURI().startsWith("file://") || triple.getObject().isLiteral()) {
						String predicateURI = triple.getPredicate().getURI();
						// if pred is text do a complex lookup in abstract index
						if (predicateURI.startsWith("http:")) {
							log.error("Cannot resolve hybrid part: " + triple.toString());
						} else {
							String localName = triple.getPredicate().getLocalName();
							// case 1: subject bound
							if (triple.getSubject().isConcrete()) {
								log.error("Cannot resolve hybrid part");
							}
							// case 2: object bound
							else if (triple.getObject().isConcrete()) {
								Node subjectVariable = triple.getSubject();
								String subjectType = getTypeOfVariable(pseudoQuery.asQuery(), subjectVariable);
								if (triple.getObject().isURI()) {
									List<Document> list = abstractsIndex.askForPredicateWithBoundAbstract(localName, triple.getObject().getURI());
									for (Document doc : list) {
										List<String> ne = extractPossibleNEFromDoc(doc, localName, triple.getObject().getURI(), subjectType);
										if (ne.size() == 1) {
											String name = "?" + subjectVariable.getName();
											// replace matching variable by
											// found
											pseudoQuery.setIri(name, ne.get(0));
										} else {
											// TODO work on this case
											log.error("Cannot resolve hybrid part");
										}
									}

								} else {
									log.error("Cannot resolve hybrid part");
								}
							}
							// case 3: both are bound
							else if (triple.getObject().isConcrete() && triple.getSubject().isConcrete()) {
								log.error("Cannot resolve hybrid part");
							}
							// case 4: neither subject nor object are bound
							else {
								log.error("Cannot resolve hybrid part");
							}
						}
						// if object is text
					} else {
						log.debug("Regular SPARQL clause: " + triple.toString());
					}
				}
			}
		}

		// if query has only variables and URIs anymore than ask DBpedia
		log.debug("\t" + pseudoQuery);
		pseudoQuery = removeUnneccessaryClauses(pseudoQuery);
		log.debug("\t" + pseudoQuery);

		// pose query to endpoint
		return sparql(pseudoQuery);
	}

	private Set<RDFNode> sparql(ParameterizedSparqlString pseudoQuery) {
		Set<RDFNode> set = Sets.newHashSet();
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", pseudoQuery.asQuery());
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				set.add(results.next().get("?uri"));
			}
		} finally {
			qexec.close();
		}
		return set;
	}

	/**
	 * does not work with more special constructs like filters etc.
	 * 
	 * @return
	 * 
	 */
	private ParameterizedSparqlString removeUnneccessaryClauses(ParameterizedSparqlString pseudoQuery) {
		ParameterizedSparqlString tmpQ = new ParameterizedSparqlString();
		tmpQ.setCommandText("SELECT ?uri WHERE {");
		List<Element> elements = ((ElementGroup) pseudoQuery.asQuery().getQueryPattern()).getElements();
		for (Element elem : elements) {
			if (elem instanceof ElementPathBlock) {
				ElementPathBlock pathBlock = (ElementPathBlock) elem;
				for (TriplePath triple : pathBlock.getPattern().getList()) {
					if ((triple.getSubject().isVariable() || triple.getObject().isVariable()) && triple.getPredicate().isURI()) {
						tmpQ.appendNode(triple.getSubject());
						tmpQ.appendNode(triple.getPredicate());
						tmpQ.appendNode(triple.getObject());

						tmpQ.append(".\n");
					}
				}
			}
		}
		tmpQ.append("}");
		return tmpQ;
	}

	/**
	 * needed when retrieving NE from full text to discard non matching ones
	 * 
	 * @param query
	 * @param variable
	 * @return
	 */
	private String getTypeOfVariable(Query query, Node variable) {
		// simple strategy: find triple where variable is in a triple with a
		// bound predicate
		List<Element> elements = ((ElementGroup) query.getQueryPattern()).getElements();
		for (Element elem : elements) {
			if (elem instanceof ElementPathBlock) {
				ElementPathBlock pathBlock = (ElementPathBlock) elem;
				for (TriplePath triple : pathBlock.getPattern().getList()) {
					Node pred = triple.getPredicate();
					// variable is in object of triple
					if (triple.getObject().equals(variable)) {
						// ask dbpedia range of pred
						String q = "select distinct ?o where { <" + pred.getURI() + "> <http://www.w3.org/2000/01/rdf-schema#range> ?o.}";
						Query sparqlQuery = QueryFactory.create(q);
						QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", sparqlQuery);
						try {
							ResultSet results = qexec.execSelect();
							while (results.hasNext()) {
								// TODO improve returning first best result
								return results.next().get("?o").asResource().getURI();
							}
						} finally {
							qexec.close();
						}
					}
					// variable is in subject of triple
					else if (triple.getSubject().equals(variable)) {
						// ask dbpedia domain of pred
						String q = "select distinct ?o  where { <" + pred.getURI() + "> <http://www.w3.org/2000/01/rdf-schema#domain> ?o.}";
						Query sparqlQuery = QueryFactory.create(q);
						QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", sparqlQuery);
						try {
							ResultSet results = qexec.execSelect();
							while (results.hasNext()) {
								// TODO improve returning first best result
								return results.next().get("?o").asResource().getURI();
							}
						} finally {
							qexec.close();
						}
					}
				}
			}
		}
		// ask variables domain respectively range
		return null;
	}

	private List<String> extractPossibleNEFromDoc(Document doc, String surrounding, String alreadyIdentifiedNE, String type) {
		// TODO use already identified NE as negative results
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
		// extract possible Named Entities (NE) via NERD modules
		ASpotter tagger = new Spotlight();
		Map<String, List<Entity>> nes = tagger.getEntities(windowText);

		// extract only NE which are contain the given type
		ArrayList<String> possibleEntitiesForVariableFoundViaTextSearch = Lists.newArrayList();
		for (String key : nes.keySet()) {
			for (Entity entity : nes.get(key)) {
				for (Resource res : entity.posTypesAndCategories) {
					String uri = res.getURI().replace("DBpedia:", "http://dbpedia.org/ontology/");
					if (uri.equals(type)) {
						possibleEntitiesForVariableFoundViaTextSearch.add(entity.uris.get(0).getURI());
					}
				}
			}
		}
		return possibleEntitiesForVariableFoundViaTextSearch;
	}
}
