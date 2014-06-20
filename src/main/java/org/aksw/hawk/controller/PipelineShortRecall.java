package org.aksw.hawk.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import javax.xml.ws.http.HTTPException;

import org.aksw.autosparql.commons.qald.QALD4_EvaluationUtils;
import org.aksw.autosparql.commons.qald.QALD_Loader;
import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.index.DBAbstractsIndex;
import org.aksw.hawk.index.IndexDBO_classes;
import org.aksw.hawk.index.IndexDBO_properties;
import org.aksw.hawk.module.Fulltexter;
import org.aksw.hawk.module.ModuleBuilder;
import org.aksw.hawk.module.PseudoQueryBuilder;
import org.aksw.hawk.module.SystemAnswerer;
import org.aksw.hawk.nlp.Pruner;
import org.aksw.hawk.nlp.SentenceToSequence;
import org.aksw.hawk.nlp.posTree.MutableTree;
import org.aksw.hawk.nlp.posTree.MutableTreeNode;
import org.aksw.hawk.nlp.spotter.ASpotter;
import org.aksw.hawk.nlp.util.CachedParseTree;
import org.aksw.hawk.pruner.GraphNonSCCPruner;
import org.aksw.hawk.pruner.QueryVariableHomomorphPruner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;

public class PipelineShortRecall {
	static Logger log = LoggerFactory.getLogger(PipelineShortRecall.class);
	String dataset;
	QALD_Loader datasetLoader;
	ASpotter nerdModule;
	CachedParseTree cParseTree;
	ModuleBuilder moduleBuilder;
	PseudoQueryBuilder pseudoQueryBuilder;
	Pruner pruner;
	SystemAnswerer systemAnswerer;
	QueryVariableHomomorphPruner queryVariableHomomorphPruner;
	GraphNonSCCPruner graphNonSCCPruner;
	Visualizer vis = new Visualizer();
	SentenceToSequence sentenceToSequence;
	Fulltexter fulltexter;

	void run() throws IOException {
		// 1. read in Questions from QALD 4
		List<Question> questions = datasetLoader.load(dataset);
		double overallf = 0;
		double overallp = 0;
		double overallr = 0;
		double counter = 0;

		for (Question q : questions) {
			// by now only work on resource questions
			if (q.answerType.equals("resource") && isSELECTquery(q.pseudoSparqlQuery, q.sparqlQuery)) {
				// log.info("->" + q.languageToQuestion);
				// 2. Disambiguate parts of the query
				q.languageToNamedEntites = nerdModule.getEntities(q.languageToQuestion.get("en"));

				// 3. Build trees from questions and cache them
				q.tree = cParseTree.process(q);
				// noun combiner, decrease #nodes in the DEPTree
				// decreases
				sentenceToSequence.combineSequences(q);
				// 4. Apply pruning rules
				q.tree = questionWordHeuristic(q);
				q.tree = pruner.prune(q);

				// log.info(q.tree.toString());

				HashMap<String, Set<RDFNode>> answer = annotateTree(q);
				// fulltexter.fulltext(q);
				for (String key : answer.keySet()) {
					Set<RDFNode> systemAnswers = answer.get(key);
					// 11. Compare to set of resources from benchmark
					double precision = QALD4_EvaluationUtils.precision(systemAnswers, q);
					double recall = QALD4_EvaluationUtils.recall(systemAnswers, q);
					double fMeasure = QALD4_EvaluationUtils.fMeasure(systemAnswers, q);
					counter++;
					if (fMeasure > 0) {
						// log.info("\tP=" + precision + " R=" + recall + " F="
						// + fMeasure);
						overallf += fMeasure;
						overallp += precision;
						overallr += recall;
					}
				}
				// break;
			}
		}
		log.info("Average P=" + overallp / counter + " R=" + overallr / counter + " F=" + overallf / counter + " Counter=" + counter);
	}

	private MutableTree questionWordHeuristic(Question q) {
		// http://dbpedia.org/ontology/Agent
		Queue<MutableTreeNode> queue = Queues.newLinkedBlockingQueue();
		queue.add(q.tree.getRoot());
		while (!queue.isEmpty()) {
			MutableTreeNode tmp = queue.poll();
			if (tmp.label.equals("Where")) {
				tmp.label = "http://dbpedia.org/ontology/Place";
				tmp.posTag = "ReplacedQuestion";
			}
			if (tmp.label.equals("Who")) {
				tmp.label = "http://dbpedia.org/ontology/Agent";
				tmp.posTag = "ReplacedQuestion";
			}
			for (MutableTreeNode n : tmp.getChildren()) {
				queue.add(n);
			}
		}
		return q.tree;
	}

	private HashMap<String, Set<RDFNode>> annotateTree(Question q) {

		HashMap<String, Set<RDFNode>> map = Maps.newHashMap();
		Set<RDFNode> set = Sets.newHashSet();

		IndexDBO_classes classesIndex = new IndexDBO_classes();
		IndexDBO_properties propertiesIndex = new IndexDBO_properties();
		Stack<MutableTreeNode> stack = new Stack<>();
		MutableTree tree = q.tree;
		stack.push(tree.getRoot().getChildren().get(0));
		while (!stack.isEmpty()) {
			MutableTreeNode tmp = stack.pop();
			String label = tmp.label;
			String posTag = tmp.posTag;

			// only one projection variable node
			if (tmp.children.size() == 0) {
				if (posTag.equals("ReplacedQuestion")) {
					// gives only hints towards the type of projection variable
				} else if (posTag.equals("CombinedNN")) {
					// fulltext lookup
					DBAbstractsIndex index = new DBAbstractsIndex();

					List<String> listAbstractsContaining = index.listAbstractsContaining(label);
					log.debug(tmp + " : " + listAbstractsContaining.size());
					for (String resourceURL : listAbstractsContaining) {
						set.add(new ResourceImpl(resourceURL));
					}
				} else if (posTag.matches("NN(.)*")) {
					// DBO look up
					if (classesIndex.search(label).size() > 0) {
						ArrayList<String> uris = classesIndex.search(label);
						for (String uri : uris) {
							// build up many trees
							QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", "SELECT ?uri WHERE {?uri a +<" + uri + ">}");
							try {
								ResultSet results = qexec.execSelect();
								while (results.hasNext()) {
									set.add(results.next().get("?uri"));
								}
							} catch (HTTPException e) {
								// TODO log.error("Query: " + pseudoQuery, e);
							} finally {
								qexec.close();
							}
						}
					} else {
						// TODO
						System.out.println("Strange case that never should happen");
					}
				} else if (posTag.equals("ADD")) {
					// strange case since entities should not be the question
					// word type
				} else {
					System.out.println("error");
				}

				break;
			} else {
				// imperative word queries like "List .." or "Give me.." do have
				// parse trees where the root is a NN(.)*
				if (posTag.matches("NN(.)*")) {
					if (classesIndex.search(label).size() > 0) {
						ArrayList<String> uris = classesIndex.search(label);
						for (String uri : uris) {
							// build up many trees
							QueryExecution qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", "SELECT ?uri WHERE {?uri a +<" + uri + ">}");
							try {
								ResultSet results = qexec.execSelect();
								while (results.hasNext()) {
									set.add(results.next().get("?uri"));
								}
							} catch (HTTPException e) {
								// TODO log.error("Query: " + pseudoQuery, e);
							} finally {
								qexec.close();
							}
						}
					}else{
						// fulltext lookup
						DBAbstractsIndex index = new DBAbstractsIndex();

						List<String> listAbstractsContaining = index.listAbstractsContaining(label);
						log.debug(tmp + " : " + listAbstractsContaining.size());
						for (String resourceURL : listAbstractsContaining) {
							set.add(new ResourceImpl(resourceURL));
						}
					}
				}

				// if X (of or P) Y which is a path in the left tree do
				// X as pred and Y as Entity (full-text or index)

				break;
			}
			// // for each VB* ask property index
			// if (posTag.matches("VB(.)*")) {
			// // System.out.println(label + " \t\t\t= " +
			// // Joiner.on(", ").join(propertiesIndex.search(label)));
			// }
			// // for each NN* ask class index
			// else if (posTag.matches("NN(.)*")) {
			// // System.out.println(label + " \t\t\t= " +
			// // Joiner.on(", ").join(classesIndex.search(label)));
			// }
			// for (MutableTreeNode child : tmp.getChildren()) {
			// stack.push(child);
			// }
			//
			// // set.add(new ResourceImpl(resourceURL));

		}
		if (set.size() < 1) {
			System.out.println(q.languageToQuestion.get("en") + "\n" + set.size());
		}
		map.put(q.languageToQuestion.get("en"), set);
		return map;
	}

	private boolean isSELECTquery(String pseudoSparqlQuery, String sparqlQuery) {
		if (pseudoSparqlQuery != null) {
			return pseudoSparqlQuery.contains("\nSELECT\n") || pseudoSparqlQuery.contains("SELECT ");
		} else if (sparqlQuery != null) {
			return sparqlQuery.contains("\nSELECT\n") || sparqlQuery.contains("SELECT ");
		}
		return false;
	}

}
