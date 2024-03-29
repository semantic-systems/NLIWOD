package org.aksw.qa.annotation.spotter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.aksw.qa.annotation.cache.PersistentCache;
import org.aksw.qa.commons.datastructure.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ASpotter {
	
	private static final Logger log = LoggerFactory.getLogger(ASpotter.class);
	private boolean useCache = false;
	private static PersistentCache cache = new PersistentCache();
	private int timeout = 60000;
	
	public abstract Map<String, List<Entity>> getEntities(String question);

	protected String requestPOST(final String input, final String requestURL, final String contentType) {

		if (useCache && cache.containsKey(input)) {
			return cache.get(input);
		}
		String output = "";
		try {
			output = post(input, requestURL, contentType);
			cache.put(input, output);
			if (useCache) {
				cache.writeCache();
			}
		} catch (MalformedURLException e) {
			log.debug("Could not call FOX for NER/NED", e);
		} catch (ProtocolException e) {
			log.debug("Could not call FOX for NER/NED", e);
		} catch (IOException e) {
			log.debug("Could not call FOX for NER/NED", e);
		}

		return output;
	}

	private String post(final String urlParameters, final String requestURL, final String contentType) throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(requestURL);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("POST");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", contentType);
		connection.setRequestProperty("Content-Length", String.valueOf(urlParameters.length()));
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();

		InputStream inputStream = connection.getInputStream();
		InputStreamReader in = new InputStreamReader(inputStream);
		BufferedReader reader = new BufferedReader(in);
		StringBuilder sb = new StringBuilder();
		while (reader.ready()) {
			sb.append(reader.readLine());
		}

		wr.close();
		reader.close();
		connection.disconnect();

		return sb.toString();
	}

	@Override
	public String toString() {
		String[] name = getClass().getName().split("\\.");
		return name[name.length - 1].substring(0, 3);
	}
}