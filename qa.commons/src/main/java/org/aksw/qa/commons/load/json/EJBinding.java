package org.aksw.qa.commons.load.json;

import org.aksw.qa.commons.utils.DateFormatter;


import com.fasterxml.jackson.annotation.JsonProperty;

public class EJBinding {
	private String datatype;
	private String type;
	private String value;
	private String xmllang;

	@Override
	public String toString() {
		return "{Type: " + type + " Value: " + value + "}";
	}

	public String getType() {
		return type;
	}

	public EJBinding setType(final String type) {
		this.type = type;
		return this;
	}

	public String getValue() {
		return value;
	}

	public EJBinding setValue(final String value) {
		if(this.datatype==null){
			this.value=value;
			return this;
		}
		switch(this.datatype){
		case "http://www.w3.org/2001/XMLSchema#boolean":
			this.value = Boolean.valueOf(value).toString();
			return this;
		case "http://www.w3.org/2001/XMLSchema#date":
			this.value = DateFormatter.formatDate(value);
			return this;
		default:
			this.value = value;
			return this;
		}
	}

	public String getDatatype() {
		return datatype;
	}

	public void setDatatype(final String datatype) {
		this.datatype = datatype;
	}

	@JsonProperty(value = "xml:lang")
	public String getXmllang() {
		return xmllang;
	}

	@JsonProperty(value = "xml:lang")
	public void setXmllang(final String xmllang) {
		this.xmllang = xmllang;
	}
}
