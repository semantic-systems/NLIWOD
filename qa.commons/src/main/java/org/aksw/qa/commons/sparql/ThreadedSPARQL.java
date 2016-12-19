
package org.aksw.qa.commons.sparql;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadedSPARQL {
	private SPARQL sparql;
	private int timeoutInSeconds = 10;
	private Logger log = LoggerFactory.getLogger(this.getClass());
	ExecutorService executor = Executors.newFixedThreadPool(1);

	public ThreadedSPARQL() {
		sparql = new SPARQL();
	}

	public ThreadedSPARQL(final int timeoutInSeconds) {
		sparql = new SPARQL();
		this.timeoutInSeconds = timeoutInSeconds;
	}

	public synchronized Set<RDFNode> sparql(final String query) throws ExecutionException {
		Callable<Set<RDFNode>> task = () -> {
			Date dateStart = new Date();
			Set<RDFNode> result = sparql.sparql(query);
			double executionTimeInS = (new Date().getTime() - (double) dateStart.getTime()) / 1000;
			DecimalFormat df = new DecimalFormat("00.000");
			log.info("Sparql response time: " + df.format(executionTimeInS) + "s");

			return result;
		};

		Future<Set<RDFNode>> future = executor.submit(task);
		Set<RDFNode> result = null;
		try {
			result = future.get(this.timeoutInSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("Sparql thread interrupted, returned null. Query: \n" + query, e);

			// } catch (ExecutionException e) {
			// log.error("Sparql class throws error in thread, returned null.
			// Query: \n" + query, e);

		} catch (TimeoutException e) {
			log.info("Query timed out after " + this.timeoutInSeconds + " s \n" + query, e);

		}
		future.cancel(true);
		return result;
	}

	public int getTimeoutInSeconds() {
		return timeoutInSeconds;
	}

	public void setTimeoutInSeconds(final int timeoutInSeconds) {
		if (timeoutInSeconds > 0) {
			this.timeoutInSeconds = timeoutInSeconds;
		}

	}

	public void destroy() {
		executor.shutdown();
	}

	public static void main(final String[] args) throws InterruptedException, ExecutionException {
		String query = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?uri ?string WHERE {         ?uri rdf:type dbo:FormulaOneRacer . 	?uri dbo:races ?x .         OPTIONAL { ?uri rdfs:label ?string. FILTER (lang(?string) = 'en') } } ORDER BY DESC(?x) OFFSET 0 LIMIT 1";
		// System.out.println(new
		// ThreadedSPARQL().sparql(LoaderController.load(Dataset.QALD6_Test_Multilingual).get(0).getSparqlQuery()).toString());
		System.out.println(new ThreadedSPARQL().sparql(query));
		System.out.println("system exit");
	}
}
