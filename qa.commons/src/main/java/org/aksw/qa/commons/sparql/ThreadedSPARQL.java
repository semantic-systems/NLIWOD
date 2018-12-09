
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

/**
 * Fire a sparql query against an endpoint
 * <p>
 * {@link SPARQL#sparql(String)} will block, if server doesn't respond. Here, you can set a maximum time limit.
 * <p>
 * This is achieved by wrapping underlying {@link SPARQL} in a Thread, which then has a maximum execution time.
 *
 * @author jhuth
 */
public class ThreadedSPARQL extends SPARQL {
	private int timeoutInSeconds = 10;
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	private ExecutorService executor = Executors.newFixedThreadPool(1);

	/**
	 * {@link #ENDPOINT_DBPEDIA_ORG} as endpoint used.
	 * <p>
	 * Default timeout : 10 seconds
	 */
	public ThreadedSPARQL() {
		super();
	}

	/**
	 * Default timeout : 10 seconds
	 */
	public ThreadedSPARQL(final String endpoint) {
		super(endpoint);
	}

	/**
	 * @param timeoutInSeconds
	 *            - set a maximum time limit for the execution of one query. Only set if >0 otherwise ignored (default=10s)
	 * @param endpoint
	 *            - A sparql endpoint, e.g. {@link SPARQLEndpoints#DBPEDIA_ORG}
	 */
	public ThreadedSPARQL(final int timeoutInSeconds, final String endpoint) {
		super(endpoint);
		if (timeoutInSeconds > 0) {
			this.timeoutInSeconds = timeoutInSeconds;
		}

	}

	/**
	 * Fire a sparql query against endpoint defined in constructor.
	 * <p>
	 * This will break operation after {@link #timeoutInSeconds} has been reached. in this case, null is returned.
	 * <p>
	 * For string representation of answers, see {@link #extractAnswerStrings(Set)}
	 *
	 * @param query
	 *            - a sparql query
	 * @return
	 * @throws ExecutionException
	 */
	@Override
	public synchronized Set<RDFNode> sparql(final String query) throws ExecutionException {
		Callable<Set<RDFNode>> task = () -> {
			Date dateStart = new Date();
			Set<RDFNode> result = super.sparql(query);

			double executionTimeInS = (new Date().getTime() - (double) dateStart.getTime()) / 1000;
			DecimalFormat df = new DecimalFormat("00.000");
			log.debug("Sparql response time: " + df.format(executionTimeInS) + "s");

			return result;
		};

		Future<Set<RDFNode>> future = executor.submit(task);
		Set<RDFNode> result = null;
		try {
			result = future.get(this.timeoutInSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new ExecutionException("Sparql thread interrupted, returned null. Query: \n" + query, e);

		} catch (TimeoutException e) {
			throw new ExecutionException("Query timed out after " + this.timeoutInSeconds + " s \n" + query, e);
		}
		future.cancel(true);
		return result;
	}

	/**
	 * @return - the time after a query times out.
	 */
	public int getTimeoutInSeconds() {
		return timeoutInSeconds;
	}

	/**
	 * @param timeoutInSeconds
	 *            - the time after a query times out.
	 */
	public void setTimeoutInSeconds(final int timeoutInSeconds) {
		if (timeoutInSeconds > 0) {
			this.timeoutInSeconds = timeoutInSeconds;
		}

	}

	/**
	 * Call this to close the underlying thread pool.
	 */
	public void destroy() {
		executor.shutdown();
	}

	public static void main(final String[] args) throws InterruptedException, ExecutionException {
		String query = "PREFIX dbo: <http://dbpedia.org/ontology/> PREFIX res: <http://dbpedia.org/resource/> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> SELECT DISTINCT ?uri ?string WHERE {         ?uri rdf:type dbo:FormulaOneRacer . 	?uri dbo:races ?x .         OPTIONAL { ?uri rdfs:label ?string. FILTER (lang(?string) = 'en') } } ORDER BY DESC(?x) OFFSET 0 LIMIT 1";
		// System.out.println(new
		// ThreadedSPARQL().sparql(LoaderController.load(Dataset.QALD6_Test_Multilingual).get(0).getSparqlQuery()).toString());
		System.out.println(new ThreadedSPARQL(90, SPARQLEndpoints.DBPEDIA_ORG).sparql(query));
		System.out.println("system exit");
	}
}
