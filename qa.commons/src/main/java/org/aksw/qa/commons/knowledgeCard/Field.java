package org.aksw.qa.commons.knowledgeCard;

import java.util.LinkedHashMap;

public class Field {
	
	private String name;
	private String value;
	private LinkedHashMap<String, String> values = null;
	private boolean isShort = true;
	
	@Override
	public String toString() {
		return "Field [name=" + name + ", value=" + value + ", values=" + values + "]";
	}

	public Field() {

	}

	public Field(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Field setName(String name) {
		this.name = name;
		return this;
	}

	public String getValue() {
		return value;
	}

	public Field setValue(String value) {
		this.value = value;
		return this;
	}

	public Field(String name, String value, boolean isShort) {
		this.name = name;
		this.value = value;
		this.isShort = isShort;
	}

	public boolean isShort() {
		return isShort;
	}

	public Field setShort(boolean aShort) {
		isShort = aShort;
		return this;
	}

	public LinkedHashMap<String, String> getValues() {
		return values;
	}

	public Field setValues(LinkedHashMap<String, String> values) {
		this.values = values;
		return this;
	}
}
