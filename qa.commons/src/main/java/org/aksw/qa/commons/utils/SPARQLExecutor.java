package org.aksw.qa.commons.utils;

import java.util.ArrayList;
import java.util.Set;

import org.aksw.qa.commons.qald.QALD4_EvaluationUtils;
import org.aksw.qa.commons.sparql.SPARQL;
import org.aksw.qa.commons.sparql.ThreadedSPARQL;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.impl.ResourceImpl;

import com.google.common.collect.Sets;

/**
 * Please consider using {@link SPARQL} or {@link ThreadedSPARQL}
 */
@Deprecated
public class SPARQLExecutor {
	
	/**
	 * An exact copy of this code is {@link SPARQL#isEndpointAlive(String)}.
	 * @param endpoint
	 * @return
	 */
	@Deprecated
	public static boolean isEndpointAlive(final String endpoint) {		
		try {
			QueryExecution qeExe = QueryExecutionFactory.sparqlService(endpoint, "PREFIX foaf:    <http://xmlns.com/foaf/0.1/> ASK  { ?x foaf:name  \"Alice\" }");
			qeExe.execAsk();	
			return true;
		} catch (Exception e) {

		}
		return false;
	}
	
	/**
	 * An exact copy of this code is {@link SPARQL#executeSelect(String)}.
	 * @param query
	 * @param endpoint
	 * @return
	 */
	@Deprecated
	public static Results executeSelect(final String query, final String endpoint) {
		QueryExecution qeExe = QueryExecutionFactory.sparqlService(endpoint, query);
		ResultSet rs = qeExe.execSelect();	
		
		Results res = new Results();
		res.header.addAll(rs.getResultVars());

		while(rs.hasNext()) {
			QuerySolution sol = rs.nextSolution();
			res.table.add(new ArrayList<String>());
			for(String head: res.header) {
				String answer = "";
				
				if(sol.get(head).isResource()) {
					answer = sol.getResource(head).toString();
				} else {
					String temp = sol.get(head).toString();
					if(temp.contains("@")) {
						answer = "\"" + temp.substring(0, temp.indexOf("@")) + "\"" + temp.substring(temp.indexOf("@"));
					} else if (temp.contains("^^")){
						answer = "\"" + temp.substring(0, temp.indexOf("^")) + "\"^^<" + temp.substring(temp.indexOf("^")+2) + ">";
					} else {
						answer = temp;
					}
				}
				res.table.get(res.table.size()-1).add(answer);
			}
		}
		return res;
	}

	/**
	 * An exact copy of this code is {@link SPARQL#executeAsk(String)}.
	 * @param query
	 * @param endpoint
	 * @return
	 */
	@Deprecated
	public static Boolean executeAsk(final String query, final String endpoint) {
		QueryExecution qeExe = QueryExecutionFactory.sparqlService(endpoint, query);
		return qeExe.execAsk();
	}

	/**
	 * An exact copy of this code is {@link SPARQL#sparql(String)}. Please consider using this, or even {@link ThreadedSPARQL}
	 *
	 * @param service
	 * @param query
	 * @return
	 */
	@Deprecated
	public static Set<RDFNode> sparql(final String service, final String query) {
		Set<RDFNode> set = Sets.newHashSet();

		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		if ((qe != null) && (query != null)) {
			if (QALD4_EvaluationUtils.isAskType(query)) {
				set.add(new ResourceImpl(String.valueOf(qe.execAsk())));
			} else {
				ResultSet results = qe.execSelect();
				String firstVarName = results.getResultVars().get(0);
				while (results.hasNext()) {

					RDFNode node = results.next().get(firstVarName);
					/**
					 * Instead of returning a set with size 1 and value (null) in it, when no answers are found, this ensures that Set is empty
					 */
					if (node != null) {
						set.add(node);
					}
				}
			}
			qe.close();
		}
		return set;
	}
}
