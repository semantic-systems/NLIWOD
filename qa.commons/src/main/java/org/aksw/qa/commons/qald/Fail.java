package org.aksw.qa.commons.qald;

/**
 * Used to annotate Errors in {@link Qald7Question}s
 *
 * @author Jonathan
 *
 */
enum Fail {
	ISONLYDBO_WRONG,
	MISSING_KEYWORDS,
	MISSING_LANGUAGES,
	ANSWERSET_DIFFERS,
	SPARQL_PARSE_ERROR,
	SPARQL_MISSING,
	SPARQL_NOT_EXECUTABLE,
	NO_ANSWERS_IN_DATASET,
	ANSWERTYPE_NOT_SET

}