package org.aksw.qa.systems;

import java.io.IOException;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.sparql.SPARQL;
import org.aksw.qa.commons.sparql.ThreadedSPARQL;
import org.apache.http.HttpResponse;
import org.apache.jena.rdf.model.RDFNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class QueGG extends Gen_HTTP_QA_Sys {

    private static final String URL = "https://scdemo.techfak.uni-bielefeld.de/quegg/query";

    public QueGG() {
        super(URL, "quegg", false, false);
        this.setQueryKey("q");
    }

    public QueGG(String url) {
        super(url, "quegg", false, false);
        this.setQueryKey("q");
    }

    @Override
    public void processQALDResp(HttpResponse response, IQuestion question)
            throws JsonParseException, JsonMappingException, UnsupportedOperationException, IOException {
        JSONParser parser = new JSONParser();
        JSONObject answerjson = null;
        String res = responseparser.responseToString(response);
        try {
            answerjson = (JSONObject) parser.parse(res);
        } catch (ParseException e) {
            e.printStackTrace();
            return;
        }

        JSONArray answers = (JSONArray) answerjson.get("results");
        if(answers.size() == 0) return;

        JSONObject answer = (JSONObject) answers.get(0);
        String query = (String) answer.get("sparql");
        question.setSparqlQuery(query);

        ThreadedSPARQL sparql = new ThreadedSPARQL();
        Set<RDFNode> queryAnswers = null;
        try {
            queryAnswers = sparql.sparql(query); 
        } finally {
           sparql.destroy(); 
        }
        question.setGoldenAnswers(SPARQL.extractAnswerStrings(queryAnswers));
    }
}