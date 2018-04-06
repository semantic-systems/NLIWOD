package org.aksw.qa.systems;

import java.util.HashSet;
import java.util.Iterator;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.http.HttpResponse;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class QUEPY extends Gen_HTTP_QA_Sys {
	private static final String SPARQL_ENDPOINT = "http://dbpedia.org/sparql";
	public QUEPY(String url) {
		super(url, "quepy", false, false);
		this.setQuery_key("question");
	}

	@Override
	public void search(IQuestion question, String language) throws Exception {
		String questionString;
		if (!question.getLanguageToQuestion().containsKey(language)) {
			return;
		}
		questionString = question.getLanguageToQuestion().get(language);
		log.debug(this.getClass().getSimpleName() + ": " + questionString);
		this.getParamMap().put(this.getQuery_key(), questionString);
		if (this.setLangPar) {
			this.getParamMap().put(this.getLang_key(), language);
		}
		HttpResponse response = this.getIsPostReq() ? fetchPostResponse() : fetchGetResponse();
		// Test if error occured
		if (response.getStatusLine().getStatusCode() >= 400) {
			throw new Exception("QUEPY Server could not answer due to: " + response.getStatusLine());
		}
		//Fetch the SPARQL
		String sparqlStr = null;
		JSONParser parser = new JSONParser();
		
		JSONObject responsejson = (JSONObject) parser.parse(responseparser
				.responseToString(response));
		JSONArray queriesArr = (JSONArray) responsejson.get("queries");
		for(int i=0;i<queriesArr.size();i++) {
			JSONObject queryObj = (JSONObject) queriesArr.get(i);
			if(queryObj.get("language").toString().equalsIgnoreCase("sparql") && queryObj.get("query")!=null) {
				sparqlStr = queryObj.get("query").toString();
				break;
			}
		}
		if(sparqlStr!=null) {
			HashSet<String> result = new HashSet<String>();
			question.setSparqlQuery(sparqlStr);
			//Fetch results using sparql
			Query query = QueryFactory.create(sparqlStr);
	        // Remote execution.
			QueryExecution qexec = QueryExecutionFactory.sparqlService(SPARQL_ENDPOINT, query) ;
			  // Set the DBpedia specific timeout.
	        ((QueryEngineHTTP)qexec).addParam("timeout", "10000") ;
	        // Execute.
	        ResultSet rs = qexec.execSelect();
	        //Get the values and push them to the question
	        while(rs.hasNext()) {
	        	QuerySolution qs = rs.next();
	        	Iterator<String> varIt = qs.varNames();
	        	while(varIt.hasNext()) {
	        		RDFNode node = qs.get(varIt.next());
	        		result.add(node.asLiteral().getString());
	        	}
	        }
	        question.setGoldenAnswers(result);
	        //ResultSetFormatter.out(System.out, rs, query);
		}
	}
}