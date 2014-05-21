package org.aksw.hawk.module;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.ws.http.HTTPException;

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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.util.FileManager;

public class SystemAnswerer {
	private static final String PROJECTION_VARIABLE = "?a0";
	private Logger log = LoggerFactory.getLogger(SystemAnswerer.class);
	private String HTTP_LIVE_DBPEDIA_ORG_SPARQL;
	private DBAbstractsIndex abstractsIndex = new DBAbstractsIndex();
	private Model rdfsModel;
	private ASpotter spotter;
	private int sizeOfWindow = 5;

	public SystemAnswerer(String endpoint, ASpotter spotter) {
		HTTP_LIVE_DBPEDIA_ORG_SPARQL = endpoint;
		this.spotter = spotter;
		this.rdfsModel = ModelFactory.createDefaultModel();
		FileManager.get().readModel(rdfsModel, new File("resources/dbpedia_3.9.owl").getAbsolutePath());
	}

	public HashMap<String, Set<RDFNode>> answer(ParameterizedSparqlString query) {
		List<ParameterizedSparqlString> targetQueries = Lists.newArrayList();

		// for each full text part of the query ask abstract index
		List<ParameterizedSparqlString> checkForFullTextTriple = checkForFullTextTriple(query);
		targetQueries.addAll(checkForFullTextTriple);
		HashMap<String, Set<RDFNode>> resultSets = Maps.newHashMap();
		for (ParameterizedSparqlString tmpQuery : targetQueries) {
			System.out.println("\t" + tmpQuery);
			// if query has only variables and URIs anymore than ask DBpedia
			tmpQuery = removeUnneccessaryClauses(tmpQuery);

			// TODO Apply rdfs reasoning on each query
			// pose query to endpoint
			Set<RDFNode> sparql = sparql(tmpQuery);
			resultSets.put(tmpQuery.toString(), sparql);
		}

		return resultSets;
	}

	private List<ParameterizedSparqlString> checkForFullTextTriple(ParameterizedSparqlString pseudoQuery) {
		List<ParameterizedSparqlString> resultQueries = Lists.newArrayList();

		List<Element> elements = ((ElementGroup) pseudoQuery.asQuery().getQueryPattern()).getElements();
		for (Element elem : elements) {
			if (elem instanceof ElementPathBlock) {
				ElementPathBlock pathBlock = (ElementPathBlock) elem;
				for (TriplePath triple : pathBlock.getPattern().getList()) {
					// only use full-text lookup if triple has full-text part
					Node subject = triple.getSubject();
					Node predicate = triple.getPredicate();
					Node object = triple.getObject();
					if (subject.isVariable() && predicate.getURI().startsWith("file://") && object.isConcrete()) {
						String localName = predicate.getLocalName();
						String subjectType = getTypeOfVariable(pseudoQuery.asQuery(), subject);
						List<Document> list = abstractsIndex.askForPredicateWithBoundAbstract(localName, object.getURI());
						for (Document doc : list) {
							log.debug("variableType " + subjectType + " variable " + subject);
							List<String> ne = extractPossibleNEFromDoc(doc, localName, object.getURI(), subjectType);
							if (ne.size() > 0) {
								// replace variable by found NE
								/*
								 * TODO bug: if variable is projection variable
								 * then a URI is the projection variable which
								 * is an error, discard query
								 */
								String name = "?" + subject.getName();
								for (int i = 0; i < ne.size(); i++) {
									ParameterizedSparqlString pss = new ParameterizedSparqlString(pseudoQuery.toString());
									if (name.equals(PROJECTION_VARIABLE)) {
										return resultQueries;
									}
									pss.setIri(name, ne.get(i));
									resultQueries.add(pss);
								}
							} else {
								log.debug("No Named Entity found for full-text lookup (possibly): " + triple);
							}
						}
					} else if (subject.isVariable() && predicate.getURI().startsWith("http://") && object.getURI().startsWith("file://")) {
						String localName = object.getLocalName();
						List<String> list = abstractsIndex.listAbstractsContaining(localName);
						String name = "?" + subject.getName();
						/*
						 * TODO bug: if variable is projection variable then a
						 * URI is the projection variable which is an error,
						 * discard query
						 */
						if (name.equals(PROJECTION_VARIABLE)) {
							return resultQueries;
						}
						for (String doc : list) {
							// replace variable by found NE
							ParameterizedSparqlString pss = new ParameterizedSparqlString(pseudoQuery.toString());
							pss.setIri(name, doc);
							resultQueries.add(pss);
						}
					} else if (subject.isVariable() && predicate.getURI().startsWith("http://") && object.isConcrete()) {
						log.debug("Nothing to do: " + pseudoQuery.toString());
					} else if (subject.isVariable() && predicate.getURI().startsWith("http://") && object.isVariable()) {
						log.debug("Nothing to do: " + pseudoQuery.toString());
					} else if (subject.isURI() && predicate.getURI().startsWith("http://") && object.isConcrete()) {
						log.debug("Nothing to do: " + pseudoQuery.toString());
					} else if (subject.isURI() && predicate.getURI().startsWith("http://") && object.isVariable()) {
						log.debug("Nothing to do: " + pseudoQuery.toString());
					} else if (subject.isURI() && predicate.getURI().startsWith("file://") && object.isVariable()) {
						String localName = predicate.getLocalName();
						Node objectVariable = object;
						String objectType = getTypeOfVariable(pseudoQuery.asQuery(), objectVariable);
						List<Document> list = abstractsIndex.askForPredicateWithBoundAbstract(localName, subject.getURI());
						for (Document doc : list) {
							log.debug("variableType " + objectType + " variable " + objectVariable);
							List<String> ne = extractPossibleNEFromDoc(doc, localName, subject.getURI(), objectType);
							if (ne.size() > 0) {
								// replace variable by found NE
								/*
								 * TODO bug: if variable is projection variable
								 * then a URI is the projection variable which
								 * is an error, discard query
								 */
								String name = "?" + objectVariable.getName();
								for (int i = 0; i < ne.size(); i++) {
									ParameterizedSparqlString pss = new ParameterizedSparqlString(pseudoQuery.toString());
									if (name.equals(PROJECTION_VARIABLE)) {
										return resultQueries;
									}
									pss.setIri(name, ne.get(i));
									resultQueries.add(pss);
								}
							} else {
								log.debug("No Named Entity found for full-text lookup (possibly): " + triple);
							}
						}
					} else if (subject.isVariable() && predicate.getURI().startsWith("file://") && object.isVariable()) {
						// TODO resolve iiieek queries
						log.debug("Not implemented case: " + triple);
					} else {
						log.warn("Not implemented case: " + triple);
					}
				}
			}
		}
		return resultQueries;
	}

	private Set<RDFNode> sparql(ParameterizedSparqlString pseudoQuery) {
		Set<RDFNode> set = Sets.newHashSet();
		QueryExecution qexec = QueryExecutionFactory.sparqlService(HTTP_LIVE_DBPEDIA_ORG_SPARQL, pseudoQuery.asQuery());
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				set.add(results.next().get(PROJECTION_VARIABLE));
			}
		} catch (HTTPException e) {
			log.error("Query: " + pseudoQuery, e);
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
		try {
			tmpQ.setCommandText("SELECT ?a0 WHERE {");
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
		} catch (QueryParseException e) {
			log.error("Query: " + pseudoQuery, e);
		}
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
		// simple strategy:
		// find triple where variable is in a triple with a bound predicate
		List<Element> elements = ((ElementGroup) query.getQueryPattern()).getElements();
		for (Element elem : elements) {
			if (elem instanceof ElementPathBlock) {
				ElementPathBlock pathBlock = (ElementPathBlock) elem;
				for (TriplePath triple : pathBlock.getPattern().getList()) {
					Node pred = triple.getPredicate();
					// variable is in object of triple
					String uri = pred.getURI();
					if (triple.getObject().equals(variable)) {
						// ask dbpedia range of pred
						return getRDFSRange(uri);
					}
					// variable is in subject of triple
					else if (triple.getSubject().equals(variable)) {
						// ask dbpedia domain of pred
						return getRDFSDomain(uri);
					}
				}
			}
		}
		// ask variables domain respectively range
		return null;
	}

	private String getRDFSDomain(String uri) {
		String q = "select distinct ?o  where { <" + uri + "> <http://www.w3.org/2000/01/rdf-schema#domain> ?o.}";
		return sparqlLocalOWL(q);
	}

	private String getRDFSRange(String uri) {
		String q = "select distinct ?o where { <" + uri + "> <http://www.w3.org/2000/01/rdf-schema#range> ?o.}";
		return sparqlLocalOWL(q);
	}

	private String sparqlLocalOWL(String sparqlQuery) {
		String targetValue = null;
		Query sparql = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.create(sparql, rdfsModel);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				targetValue = results.next().get("?o").asResource().getURI();
				break;
			}
		} finally {
			qexec.close();
		}
		return targetValue;
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
		Map<String, List<Entity>> nes = spotter.getEntities(windowText);

		// extract only NE which are contain the given type
		ArrayList<String> possibleEntitiesForVariableFoundViaTextSearch = Lists.newArrayList();
		for (String key : nes.keySet()) {
			for (Entity entity : nes.get(key)) {
				// TODO replace this by looking for type inside types.ttl of
				// DBpedia but(!) loading instance.ttl to jena takes too long
				for (String uri : getTypes(entity.uris.get(0).getURI())) {
					// String uri = res.getURI().replace("DBpedia:",
					// "http://dbpedia.org/ontology/");
					if (uri.equals(type)) {
						possibleEntitiesForVariableFoundViaTextSearch.add(entity.uris.get(0).getURI());
					}
				}
			}
		}
		log.debug("#Possible Entities:" + possibleEntitiesForVariableFoundViaTextSearch.size());
		return possibleEntitiesForVariableFoundViaTextSearch;
	}

	private List<String> getTypes(String uri) {
		String q = "select distinct " + PROJECTION_VARIABLE + " where { <" + uri + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> " + PROJECTION_VARIABLE + ".}";
		ParameterizedSparqlString pseudoQuery = new ParameterizedSparqlString(q);
		List<String> types = Lists.newArrayList();
		for (RDFNode node : sparql(pseudoQuery)) {
			types.add(node.asResource().getURI());
		}
		return types;
	}

	public static void main(String args[]) {
		String q = "SELECT ?a0 WHERE { " + "?a0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Settlement> . " + "?a1 <http://dbpedia.org/ontology/birthPlace> ?a0 . " + "?a1 <assassin> <http://dbpedia.org/resource/Martin_Luther_King%2C_Jr.> .}";
		ParameterizedSparqlString pss = new ParameterizedSparqlString(q);

		SystemAnswerer sys = new SystemAnswerer("http://dbpedia.org/sparql", new Spotlight());

		HashMap<String, Set<RDFNode>> ans = sys.answer(pss);
		for (String key : ans.keySet()) {
			System.out.println(key);
			for (RDFNode rdfNode : ans.get(key)) {
				System.out.println("\t->" + rdfNode);
			}
		}
	}
}
