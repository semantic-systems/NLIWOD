package org.aksw.qa.systems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.apache.http.HttpResponse;

public abstract class ASystem {

	abstract HashSet<String> search(String question);

	String responseToString(HttpResponse response) throws IllegalStateException, IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		// TODO use java 8 file IO API
		StringBuffer htmlResponse = new StringBuffer();
		String line = "";
		while ((line = br.readLine()) != null) {
			htmlResponse.append(line).append("\n");
		}
		return htmlResponse.toString();
	}

}
