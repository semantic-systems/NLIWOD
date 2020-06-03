package org.aksw.qa.commons.nlp.nerd;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * URI Disambiguation using AGDISTIS https://github.com/AKSW/AGDISTIS
 * 
 * @author r.usbeck
 * 
 */

public class AGDISTIS {
	/**
	 * 
	 * @param inputText
	 *            with encoded entities,e.g.,
	 *            "<entity> Barack </entity> meets <entity>Angela</entity>"
	 * @return map of string to disambiguated URL
	 * @throws ParseException
	 * @throws IOException
	 */
	public HashMap<String, String> runDisambiguation(String inputText) throws ParseException, IOException {
		String urlParameters = "text=" + URLEncoder.encode(inputText, "UTF-8");
		urlParameters += "&type=agdistis";

		// change this URL to https://agdistis.demos.dice-research.org/api/zh_cn/ to use
		// chinese endpoint
		URL url = new URL("https://agdistis.demos.dice-research.org/api/en/");
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", String.valueOf(urlParameters.length()));

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

		String agdistis = sb.toString();

		JSONParser parser = new JSONParser();
		JSONArray resources = (JSONArray) parser.parse(agdistis);

		HashMap<String, String> tmp = new HashMap<String, String>();
		for (Object res : resources.toArray()) {
			JSONObject next = (JSONObject) res;
			String namedEntity = (String) next.get("namedEntity");
			String disambiguatedURL = (String) next.get("disambiguatedURL");
			tmp.put(namedEntity, disambiguatedURL);
		}
		return tmp;
	}
}
