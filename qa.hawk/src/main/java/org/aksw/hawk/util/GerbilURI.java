package org.aksw.hawk.util;

import java.util.Objects;
import java.util.Vector;

import org.aksw.qa.commons.load.json.EJBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GerbilURI {
	private Logger log = LoggerFactory.getLogger(GerbilURI.class);
	private String type;
	private String value;
	
	public GerbilURI() {
		type = new String();
		value = new String();
	}
	
	public GerbilURI setType(final String type) {
		this.type = type;
		return this;
	}
	
	public GerbilURI setValue(final String value) {
		this.value = value;
		return this;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getValue() {
		return this.value;
	}
	@Override
	public String toString() {
		return "\n    type: " + type + "\n    value: " + value;
	}
	
}
