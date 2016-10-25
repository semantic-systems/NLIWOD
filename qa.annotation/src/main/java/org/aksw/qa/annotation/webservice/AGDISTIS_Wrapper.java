package org.aksw.qa.annotation.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.aksw.gerbil.io.nif.NIFParser;
import org.aksw.gerbil.io.nif.NIFWriter;
import org.aksw.gerbil.io.nif.impl.TurtleNIFParser;
import org.aksw.gerbil.io.nif.impl.TurtleNIFWriter;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Span;
import org.aksw.qa.annotation.util.AgdistisJson;
import org.aksw.qa.annotation.util.NifEverything;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/agdistis")
public class AGDISTIS_Wrapper {
	private NifEverything nif = NifEverything.getInstance();
	private final static String AGDISTIS_URL = "http://139.18.2.164:8080/AGDISTIS";
	final Logger logger = LoggerFactory.getLogger(this.getClass());

	public AGDISTIS_Wrapper() {

	}

	public String agdistis_post(final String input) throws IOException {
		String body = "text=" + URLEncoder.encode("'" + input + "'", "UTF-8") + "&type=agdistis";

		URL url = new URL(AGDISTIS_URL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", String.valueOf(body.length()));

		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
		writer.write(body);
		writer.flush();

		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		StringBuilder out = new StringBuilder();
		int i = reader.read();

		while (i != -1) {
			out.append((char) i);
			i = reader.read();
		}

		writer.close();
		reader.close();
		return out.toString();
	}

	public String requestWordUrl(final String word) {
		String agdistisReturn = null;
		try {
			agdistisReturn = agdistis_post("<entity>" + word + "</entity>");
		} catch (IOException e) {
			logger.info("Prolem reaching AGDISITS", e);
		}

		ObjectMapper mapper = new ObjectMapper();

		JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, AgdistisJson.class);

		List<AgdistisJson> returnList = new ArrayList<>();
		try {
			returnList = mapper.readValue(agdistisReturn, type);
		} catch (IOException e) {
			logger.info("Prolem parsing AGDISTIS results", e);
		}

		switch (returnList.size()) {
		case 0:
			return null;
		case 1:
			return returnList.get(0).getDisambiguatedURL();
		default:
			logger.debug("Got more than one URI from AGDISTIS for one single Phrase. Returning null.");
			return null;

		}

	}

	@RequestMapping(method = RequestMethod.GET)
	public String getAgdistis(@RequestParam(value = "q") final String q) {
		logger.debug("Requesting CLASS search for term (VIA GET) :|" + q + "|");
		return null;

	}

	@RequestMapping(method = RequestMethod.POST)
	public String postClass(@RequestBody final String input) {
		logger.debug("Requesting CLASS search for term (VIA POST) ");
		logger.trace("|" + input + "|");
		return null;
	}

	private String annotateNIFWord(final String nifString) {
		List<Document> docs = nif.parseNIF(nifString);

		for (Document doc : docs) {
			for (Span span : doc.getMarkings(Span.class)) {
				// if(span.g)

			}

		}
		return null;
	}

	public static void main(final String[] args) throws Exception {
		System.out.println("Started agdistis wrapper");
		AGDISTIS_Wrapper wrapper = new AGDISTIS_Wrapper();
		// System.out.println("|" + wrapper.agdistis_post("<entity>Barack
		// Obama</entity> arrives in <entity>Washington, D.C.</entity>.") +
		// "|");
		System.out.println(wrapper.requestWordUrl("Black Hawk"));

		// File f = new File("c:/output/nifexample.txt");
		// FileReader reader = new FileReader(f);
		// BufferedReader buff = new BufferedReader(reader);
		// StringBuilder str = new StringBuilder();
		// while (buff.ready()) {
		// str.append((char) buff.read());
		// }
		//
		// List<Document> docs = parseNIF(str.toString());
		// for (Document d : docs) {
		// System.out.println(d.toString());
		// }

		// Document doc = new DocumentImpl("qwe");
		// Marking m = new SpanImpl(0, 1);
		// doc.setText("awesd");
		// doc.addMarking(m);
		// List<Document> docs = new ArrayList<>();
		// docs.add(doc);
		// System.out.println(writeNIF(docs));

	}

	private static String writeNIF(final List<Document> docs) {
		NIFWriter writer = new TurtleNIFWriter();
		return writer.writeNIF(docs);

	}

	private static List<Document> parseNIF(final String input) throws IllegalArgumentException {
		NIFParser parser = new TurtleNIFParser();
		List<Document> docs = parser.parseNIF(input);
		/**
		 * Had machine-dependent paths here
		 */
		for (Document doc : docs) {
			doc.setDocumentURI(null);
		}
		if (CollectionUtils.isEmpty(docs)) {

			throw new IllegalArgumentException("Nothing parsed");
		}
		return docs;

	}

}
