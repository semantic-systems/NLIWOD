package org.aksw.qa.commons.sparql;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.RDFNode;

/**
 * Fires {@link IQuestions} against an endpoint and sets the endpoint's answers as golden answers.
 *
 * @author jhuth
 */
public class AnswerSyncer {
	/**
	 * Retrieves answers for the sparql in this IQuestion from given endpoint, and sets it as goldenAnswers.
	 * <p>
	 * If the resultSet of server is null(e.g. sparql timed out) golden answers will be an empty Set.
	 * <p>
	 * Using {@link #syncAnswers(List, String)} for multiple questions may increase performance.
	 *
	 * @param q
	 * @param endpoint
	 *            an endpoint e.g. {@link SPARQLEndpoints#DBPEDIA_ORG}
	 * @throws ExecutionException
	 *             e.g. server error, times out, thread gets interrupted....
	 */
	public static void syncAnswers(final IQuestion q, final String endpoint) throws ExecutionException {
		syncAnswers(q, endpoint, -1);

	}

	/**
	 * Retrieves answers for the sparql in this IQuestion from given endpoint, and sets it as goldenAnswers.
	 * <p>
	 * If the resultSet of server is null(e.g. sparql timed out) golden answers will be an empty Set.
	 * <p>
	 * Using {@link #syncAnswers(List, String)} for multiple questions may increase performance.
	 *
	 * @param q
	 * @param endpoint
	 *            an endpoint e.g. {@link SPARQLEndpoints#DBPEDIA_ORG}
	 * @param queryTimeoutInSecs
	 *            Self-explanatory. if ! > 0 it will be ignored. (default 10secs)
	 * @throws ExecutionException
	 *             e.g. server error, times out, thread gets interrupted....
	 */
	public static void syncAnswers(final IQuestion q, final String endpoint, final int queryTimeoutInSecs) throws ExecutionException {
		ThreadedSPARQL sp = new ThreadedSPARQL(queryTimeoutInSecs, endpoint);
		try {
			answer(sp, q);
		} finally {
			sp.destroy();
		}

	}

	/**
	 * Retrieves answers for the sparql in all IQuestions from given endpoint, and sets it as goldenAnswers.
	 * <p>
	 * If the resultSet of server is null(e.g. sparql timed out) golden answers will be an empty Set.
	 *
	 * @param q
	 * @param endpoint
	 *            an endpoint e.g. {@link SPARQLEndpoints#DBPEDIA_ORG}
	 * @param queryTimeoutInSecs
	 *            Self-explanatory. if ! > 0 it will be ignored. (default 10secs)
	 * @throws ExecutionException
	 *             e.g. server error, times out, thread gets interrupted....
	 */
	public static void syncAnswers(final List<IQuestion> questions, final String endpoint) throws ExecutionException {
		syncAnswers(questions, endpoint, -1);

	}

	/**
	 * Retrieves answers for the sparql in all IQuestions from given endpoint, and sets it as goldenAnswers.
	 * <p>
	 * If the resultSet of server is null(e.g. sparql timed out) golden answers will be an empty Set.
	 *
	 * @param q
	 * @param endpoint
	 *            an endpoint e.g. {@link SPARQLEndpoints#DBPEDIA_ORG}
	 * @throws ExecutionException
	 *             e.g. server error, times out, thread gets interrupted....
	 */
	public static void syncAnswers(final List<IQuestion> questions, final String endpoint, final int queryTimeoutInSecs) throws ExecutionException {
		ThreadedSPARQL sp = new ThreadedSPARQL(queryTimeoutInSecs, endpoint);
		try {
			for (IQuestion q : questions) {
				answer(sp, q);
			}
		} finally {
			sp.destroy();
		}

	}

	private static void answer(final ThreadedSPARQL sp, final IQuestion q) throws ExecutionException {
		String query = q.getSparqlQuery();
		Set<RDFNode> answer = sp.sparql(query);
		if (answer != null) {
			q.setGoldenAnswers(SPARQL.extractAnswerStrings(answer));
		} else {
			q.setGoldenAnswers(Sets.newHashSet());
		}
	}

}
