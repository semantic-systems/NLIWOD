package org.aksw.qa.commons.load.extended;

public class EJBinding {

	private String type;
	private String value;

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
		this.value = value;
		return this;
	}
}
