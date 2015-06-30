package org.aksw.hawk.webservice;

import java.util.UUID;

import org.apache.jena.atlas.json.JsonString;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class SearchIdException extends RuntimeException {

	public SearchIdException(UUID UUID) {
		super(constructException(UUID));
	}

	@SuppressWarnings("unchecked")
	private static String constructException(UUID UUID) {
		JSONObject obj = new JSONObject();
		obj.put("error", "No such search id");
		obj.put("UUID", new JsonString(UUID.toString()));
		return obj.toJSONString();
	}
}