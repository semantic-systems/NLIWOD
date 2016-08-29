package org.aksw.qa.commons.load.stanford;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.aksw.jena_sparql_api.http.QueryExecutionHttpWrapper;
import org.aksw.qa.commons.datastructure.Entity;
import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.LoaderController;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.util.iterator.Filter;
import org.apache.jena.vocabulary.RDFS;
import org.dllearner.algorithms.qtl.QueryTreeUtils;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGenerator;
import org.dllearner.algorithms.qtl.operations.lgg.LGGGeneratorSimple;
import org.dllearner.algorithms.qtl.util.StopURIsDBpedia;
import org.dllearner.algorithms.qtl.util.StopURIsOWL;
import org.dllearner.algorithms.qtl.util.StopURIsRDFS;
import org.dllearner.algorithms.qtl.util.StopURIsSKOS;
import org.dllearner.algorithms.qtl.util.filters.NamespaceDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.ObjectDropStatementFilter;
import org.dllearner.algorithms.qtl.util.filters.PredicateDropStatementFilter;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGenerator;
import org.dllearner.kb.sparql.ConciseBoundedDescriptionGeneratorImpl;
import org.dllearner.kb.sparql.SymmetricConciseBoundedDescriptionGeneratorImpl;
import org.json.simple.parser.ParseException;

//TODO actually refactor this class to an own submodule
public class StanfordRDFizer {

	private AGDISTIS disambiguator;
	private Spotlight recognizer;

	public StanfordRDFizer() {
		this.disambiguator = new AGDISTIS();
		this.recognizer = new Spotlight();
	}

	public Map<String, List<Entity>> recognize(String question) {
		return this.recognizer.getEntities(question);
	}

	public String disambiguate(String label) {

		String preAnnotatedText = "<entity>" + label + "</entity>";

		HashMap<String, String> results;
		try {
			results = disambiguator.runDisambiguation(preAnnotatedText);
			for (String namedEntity : results.keySet()) {
				return results.get(namedEntity);
			}
		} catch (ParseException | IOException e) {
			// TODO build in proper logging
			e.printStackTrace();
		}
		return null;
	}


}
