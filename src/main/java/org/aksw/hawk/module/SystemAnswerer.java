package org.aksw.hawk.module;

import java.net.URL;
import java.util.ArrayList;
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
	private Model typesModel;

	public SystemAnswerer(String endpoint, ASpotter spotter) {
		// TODO resolve hack
		this.spotter = spotter;
		// this.spotter = new Spotlight();
		HTTP_LIVE_DBPEDIA_ORG_SPARQL = endpoint;
		URL url = this.getClass().getClassLoader().getResource("dbpedia_3.9.owl");
		this.rdfsModel = ModelFactory.createDefaultModel();
		FileManager.get().readModel(rdfsModel, url.getFile());
//		url = this.getClass().getClassLoader().getResource("instance_types_en.ttl");
//		this.typesModel = ModelFactory.createDefaultModel();
//		FileManager.get().readModel(typesModel, url.getFile());
	}

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
							log.warn("Cannot resolve hybrid part: " + triple.toString());
						} else {
							String localName = triple.getPredicate().getLocalName();
							// case 1: subject bound
							if (triple.getSubject().isConcrete()) {
								log.warn("Cannot resolve hybrid part");
							}
							// case 2: object bound
							else if (triple.getObject().isConcrete()) {
								Node subjectVariable = triple.getSubject();
								String subjectType = getTypeOfVariable(pseudoQuery.asQuery(), subjectVariable);
								if (triple.getObject().isURI()) {
									List<Document> list = abstractsIndex.askForPredicateWithBoundAbstract(localName, triple.getObject().getURI());
									for (Document doc : list) {
										log.debug("variableType " + subjectType + " variable " + subjectType);
										List<String> ne = extractPossibleNEFromDoc(doc, localName, triple.getObject().getURI(), subjectType);
										if (ne.size() == 1) {
											String name = "?" + subjectVariable.getName();
											// replace variable by found NE
											/*
											 * TODO bug: if variable is
											 * projection variable then a URI is
											 * the projection variable which is
											 * an error, discard query
											 */
											if (name.equals(PROJECTION_VARIABLE)) {
												return Sets.newHashSet();
											}
											pseudoQuery.setIri(name, ne.get(0));
										} else {
											// TODO!!!! work on this case
											log.warn("Cannot resolve hybrid part");
										}
									}

								} else {
									log.warn("Cannot resolve hybrid part");
								}
							}
							// case 3: both are bound
							else if (triple.getObject().isConcrete() && triple.getSubject().isConcrete()) {
								log.warn("Cannot resolve hybrid part");
							}
							// case 4: neither subject nor object are bound
							else {
								log.warn("Cannot resolve hybrid part");
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
		pseudoQuery = removeUnneccessaryClauses(pseudoQuery);
		log.debug("\t" + pseudoQuery);

		// pose query to endpoint
		// TODO Apply rdfs reasoning on each query

		return sparql(pseudoQuery);
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
				// DBpedia
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
		if(possibleEntitiesForVariableFoundViaTextSearch.size()>1){
			System.out.println();
		}
		return possibleEntitiesForVariableFoundViaTextSearch;
	}

	// TODO check behaviour
	private List<String> getTypes(String uri) {
		String q = "select distinct ?o where { <" + uri + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?o.}";
		ParameterizedSparqlString pseudoQuery = new ParameterizedSparqlString(q);
		Set<RDFNode> set = Sets.newHashSet();
		QueryExecution qexec = QueryExecutionFactory.sparqlService(HTTP_LIVE_DBPEDIA_ORG_SPARQL, pseudoQuery.asQuery());
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				set.add(results.next().get("?o"));
			}
		} catch (HTTPException e) {
			log.error("Query: " + pseudoQuery, e);

		} finally {
			qexec.close();
		}
		List<String> types = Lists.newArrayList();
		for (RDFNode node : set) {
			types.add(node.asResource().getURI());
		}
		return types;
	}

	private List<String> sparqlLocalTypes(String sparqlQuery) {
		List<String> types = Lists.newArrayList();
		Query sparql = QueryFactory.create(sparqlQuery);
		QueryExecution qexec = QueryExecutionFactory.create(sparql, typesModel);
		try {
			ResultSet results = qexec.execSelect();
			while (results.hasNext()) {
				types.add(results.next().get("?o").asResource().getURI());
			}
		} finally {
			qexec.close();
		}
		return types;
	}

	public static void main(String args[]) {
		String q = "SELECT ?a0 WHERE { " + "?a0 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://dbpedia.org/ontology/Settlement> . " + "?a1 <http://dbpedia.org/ontology/birthPlace> ?a0 . " + "?a1 <assassin> <http://dbpedia.org/resource/Martin_Luther_King%2C_Jr.> .}";
		ParameterizedSparqlString pss = new ParameterizedSparqlString(q);

		SystemAnswerer sys = new SystemAnswerer("http://dbpedia.org/sparql", new Spotlight());

		Set<RDFNode> ans = sys.answer(pss);
		for (RDFNode answer : ans) {
			System.out.println(answer);
		}
	}
}
