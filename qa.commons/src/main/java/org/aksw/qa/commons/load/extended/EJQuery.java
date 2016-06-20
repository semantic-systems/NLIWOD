package org.aksw.qa.commons.load.extended;

public class EJQuery {

	private String schemaless;
	private String SPARQL;

	public String getSchemaless() {
		return schemaless;
	}

	public EJQuery setSchemaless(final String schemaless) {
		this.schemaless = schemaless;
		return this;
	}

	public String getSPARQL() {
		return SPARQL;
	}

	public EJQuery setSPARQL(final String sPARQL) {
		SPARQL = sPARQL;
		return this;
	}

}
