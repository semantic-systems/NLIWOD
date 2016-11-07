package org.aksw.qa.commons.load.json;

public class QaldQuery {
	
	private String sparql;
	private String pseudo;

	public String getSparql() {
		return sparql;
	}

	public String getPseudo() {
		return pseudo;
	}

	public void setSparql(final String sparql) {
		//TODO validate SPARQL Query
		this.sparql = sparql;
	}

	public void setPseudo(final String pseudo) {
		this.pseudo = pseudo;
	}

}
