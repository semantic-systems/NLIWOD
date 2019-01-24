package org.aksw.qa.annotation.webservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.gerbil.transfer.nif.Marking;
import org.aksw.gerbil.transfer.nif.Span;
import org.aksw.gerbil.transfer.nif.data.DocumentImpl;
import org.aksw.qa.annotation.util.AgdistisJson;
import org.aksw.qa.annotation.util.NifEverything;
import org.aksw.qa.annotation.util.NifEverything.NifProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/agdistis")
public class AgdistisWrapper {
	private NifEverything nif = NifEverything.getInstance();
	private final static String AGDISTIS_URL = "http://139.18.2.164:8080/AGDISTIS";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@RequestMapping(method = RequestMethod.GET)
	public String getAgdistis() {
		logger.debug("Requesting AGDISTIS VIA GET - forbidden");
		return "GET forbidden";

	}

	@RequestMapping(method = RequestMethod.POST)
	public String postAgdistis(@RequestBody final String input) {
		logger.debug("Requesting agdistis search  (VIA POST)");
		logger.trace("|" + input + "|");
		return process(input);
	}

	public String process(final String input) {
		List<Document> docs;
		try {
			docs = nif.parseNIF(input);
		} catch (Exception e) {
			return NifEverything.INPUT_NOT_PARSABLE;
		}

		List<Document> answerDocs = new ArrayList<>();

		for (Document doc : docs) {
			Document answerDoc = new DocumentImpl(doc.getText());
			List<Marking> resultMarkings = requestForStringAgdistis(createAgdistisString(doc));
			nif.addAllMarkingsToDoc(answerDoc, resultMarkings);
			answerDocs.add(answerDoc);
		}

		return nif.writeNIF(answerDocs);
	}

	private String agdistisPost(final String input) throws IOException {
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

	public List<Marking> requestForStringAgdistis(final String sentence) {
		ArrayList<Marking> ret = new ArrayList<>();
		String agdistisReturn = null;
		try {
			agdistisReturn = agdistisPost(sentence);
		} catch (IOException e) {
			logger.info("Prolem reaching AGDISITS", e);
			return new ArrayList<>();
		}

		ObjectMapper mapper = new ObjectMapper();

		JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, AgdistisJson.class);

		List<AgdistisJson> returnList = new ArrayList<>();
		try {
			returnList = mapper.readValue(agdistisReturn, type);
		} catch (IOException e) {
			logger.info("Prolem parsing AGDISTIS results", e);
		}

		for (AgdistisJson result : returnList) {
			Marking span = NifProperty.TAIDENTREF.getInstanceWith(result.getStart() - 1, result.getOffset(), new ArrayList<>(Arrays.asList(result.getDisambiguatedURL())));
			ret.add(span);
		}
		return ret;
	}

	public String createAgdistisString(final Document doc) {
		/**
		 * Getting all Markings with "nif:word" Annotation
		 */
		List<Span> spans = new ArrayList<>();
		for (Span span : doc.getMarkings(Span.class)) {
			if (span.getIsWord()) {
				spans.add(span);
			}

		}
		/**
		 * Sorting them so order in array is same order as they appear in
		 * sentence.
		 */
		Collections.sort(spans, new Comparator<Span>() {

			@Override
			public int compare(final Span o1, final Span o2) {
				return o1.getStartPosition() - o2.getStartPosition();
			}

		});

		List<Span> notProcessedSpans = new ArrayList<>(spans);
		/**
		 * Getting original Question
		 */
		StringBuilder docStr = new StringBuilder(doc.getText());

		for (Span span : spans) {
			notProcessedSpans.remove(span);
			int spanStart = span.getStartPosition();
			int spanLength = span.getLength();
			/**
			 * Sadly, not possible to get "nif:ancherOf" so we need this to
			 * extract the word of this annotation
			 */
			String word = docStr.substring(spanStart, spanStart + spanLength);
			/**
			 * Annotating word for Agdistis
			 */
			String insertionString = "<entity>" + word + "</entity>";
			/**
			 * Replacing the word with insertionString makes Offsets of
			 * following Spans invalid, so calculate new Offset and insert it to
			 * the other nodes.
			 */
			int offsetForFollowing = insertionString.length() - word.length();
			for (Span it : notProcessedSpans) {
				it.setStartPosition(it.getStartPosition() + offsetForFollowing);
			}
			/**
			 * Replacing annotated word with insertionString
			 */
			docStr.replace(spanStart, spanStart + spanLength, insertionString);

		}

		return docStr.toString();
	}

}
