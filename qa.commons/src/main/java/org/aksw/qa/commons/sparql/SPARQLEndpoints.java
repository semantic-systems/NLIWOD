package org.aksw.qa.commons.sparql;

public class SPARQLEndpoints {
	public static final String DBPEDIA_ORG = "http://dbpedia.org/sparql";
	public static final String WIKIDATA_ORG = "http://query.wikidata.org/sparql";
	/**
	 * Be sure to import the SSL certificate from the metaphacts site to your local JRE certificate libary:
	 * http://stackoverflow.com/questions/6659360/how-to-solve-javax-net-ssl-sslhandshakeexception-error
	 * http://superuser.com/questions/97201/how-to-save-a-remote-server-ssl-certificate-locally-as-a-file
	 */
	public static final String WIKIDATA_METAPHACTS = "https://wikidata.metaphacts.com/sparql";
}
