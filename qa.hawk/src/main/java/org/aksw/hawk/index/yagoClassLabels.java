package org.aksw.hawk.index;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.aksw.jena_sparql_api.http.QueryExecutionFactoryHttp;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;

public class yagoClassLabels {

	public static void main(final String args[]) throws IOException {
		String endpoint = "http://lod.openlinksw.com/sparql";

		QueryExecutionFactory qef = new QueryExecutionFactoryHttp(endpoint);

		BufferedWriter bw = new BufferedWriter(new FileWriter("yagoClassLabel.ttl"));
		for (int LIMIT = 100000;; LIMIT += 100000) {
			String query = "select distinct ?class ?label" + "			where {" + "			?class <http://www.w3.org/2000/01/rdf-schema#label> ?label."
			        + "			?class <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?topClass." + "			FILTER(STRSTARTS(STR(?class), \"http://yago-knowledge.org/resource/\"))"
			        + "           FILTER( langMatches( lang(?label), \"en\" )|| langMatches( lang(?label), \"\" ))" + "			}" + "         LIMIT " + LIMIT + "         OFFSET " + (LIMIT - 100000);
			QueryExecution qe = qef.createQueryExecution(query);
			ResultSet rs = qe.execSelect();
			int i = 0;
			while (rs.hasNext()) {
				bw.write(rs.next().toString());
				bw.newLine();
				i++;
			}
			if (i < 99999) {
				break;
			}
		}
		bw.close();

	}
}
