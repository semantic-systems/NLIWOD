package org.aksw.mlqa.experiment;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.load.Dataset;
import org.aksw.qa.commons.load.QALD_Loader;
import org.aksw.qa.commons.measure.SPARQLBasedEvaluation;
import org.aksw.qa.commons.utils.SPARQLExecutor;
import org.aksw.qa.systems.HAWK;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

import com.google.common.collect.Sets;

import junit.framework.Assert;

public class RunProducerTest {

	@Test
	public void test() {
		JSONArray arr = RunProducer.loadRunData(Dataset.QALD6_Train_Multilingual);
		List<IQuestion> trainQuestions = QALD_Loader.load(Dataset.QALD6_Train_Multilingual);

		JSONObject obj = (JSONObject) arr.get(1);
		JSONObject allsystemdata = (JSONObject) obj.get("answers");
		JSONObject systemdata = (JSONObject) allsystemdata.get("hawk");
		System.out.println(obj.toJSONString());
		System.out.println(new Double(systemdata.get("fmeasure").toString()));
		String question = obj.get("question").toString();
		System.out.println(allsystemdata.get("hawk"));
		assertTrue(trainQuestions.get(1).getLanguageToQuestion().get("en").equals(question));	
	}

}
