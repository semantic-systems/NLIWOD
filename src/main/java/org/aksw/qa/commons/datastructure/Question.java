package org.aksw.qa.commons.datastructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.utils.CollectionUtils;

public class Question implements IQuestion {

	private Integer id;
	private String answerType;
	private String pseudoSparqlQuery;
	private String sparqlQuery;
	private Boolean aggregation;
	private Boolean onlydbo;
	private Boolean outOfScope;
	private Boolean hybrid;
	private Map<String, String> languageToQuestion = CollectionUtils.newLinkedHashMap();
	private Map<String, List<String>> languageToKeywords = CollectionUtils.newLinkedHashMap();
	private Set<String> goldenAnswers = CollectionUtils.newHashSet();

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#toString()
	 */
	@Override
	public String toString() {
		return "Question [id=" + id + ", answerType=" + answerType + ", aggregation=" + aggregation + ", onlydbo=" + onlydbo + ", outOfScope=" + outOfScope + ", hybrid=" + hybrid
		        + ", pseudoSparqlQuery=" + pseudoSparqlQuery + ", sparqlQuery=" + sparqlQuery + ", languageToQuestion=" + languageToQuestion + ", languageToKeywords=" + languageToKeywords
		        + ", goldenAnswers=" + goldenAnswers + "]";
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setValue(java.lang.String, java.lang.String)
	 */
	@Override
    public void setValue(String valDescriptor, String val) {
		valDescriptor = valDescriptor.toLowerCase();
		switch (valDescriptor) {
		case "id":
			this.id = Integer.parseInt(val);
			break;
		case "answertype":
			this.answerType = val;
			break;
		case "aggregation":
			this.aggregation = Boolean.parseBoolean(val);
			break;
		case "onlydbo":
			this.onlydbo = Boolean.parseBoolean(val);
			break;
		case "hybrid":
			this.hybrid = Boolean.parseBoolean(val);
			break;
		default:
			;
		}
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getId()
	 */
	@Override
    public Integer getId() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setId(java.lang.Integer)
	 */
	@Override
    public void setId(Integer id) {
		this.id = id;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getAnswerType()
	 */
	@Override
    public String getAnswerType() {
		return answerType;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setAnswerType(java.lang.String)
	 */
	@Override
    public void setAnswerType(String answerType) {
		this.answerType = answerType;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getPseudoSparqlQuery()
	 */
	@Override
    public String getPseudoSparqlQuery() {
		return pseudoSparqlQuery;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setPseudoSparqlQuery(java.lang.String)
	 */
	@Override
    public void setPseudoSparqlQuery(String pseudoSparqlQuery) {
		this.pseudoSparqlQuery = pseudoSparqlQuery;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getSparqlQuery()
	 */
	@Override
    public String getSparqlQuery() {
		return sparqlQuery;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setSparqlQuery(java.lang.String)
	 */
	@Override
    public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getAggregation()
	 */
	@Override
    public Boolean getAggregation() {
		return aggregation;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setAggregation(java.lang.Boolean)
	 */
	@Override
    public void setAggregation(Boolean aggregation) {
		this.aggregation = aggregation;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getOnlydbo()
	 */
	@Override
    public Boolean getOnlydbo() {
		return onlydbo;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setOnlydbo(java.lang.Boolean)
	 */
	@Override
    public void setOnlydbo(Boolean onlydbo) {
		this.onlydbo = onlydbo;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getOutOfScope()
	 */
	@Override
    public Boolean getOutOfScope() {
		return outOfScope;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setOutOfScope(java.lang.Boolean)
	 */
	@Override
    public void setOutOfScope(Boolean outOfScope) {
		this.outOfScope = outOfScope;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getHybrid()
	 */
	@Override
    public Boolean getHybrid() {
		return hybrid;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setHybrid(java.lang.Boolean)
	 */
	@Override
    public void setHybrid(Boolean hybrid) {
		this.hybrid = hybrid;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getLanguageToQuestion()
	 */
	@Override
    public Map<String, String> getLanguageToQuestion() {
		return languageToQuestion;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setLanguageToQuestion(java.util.Map)
	 */
	@Override
    public void setLanguageToQuestion(Map<String, String> languageToQuestion) {
		this.languageToQuestion = languageToQuestion;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getLanguageToKeywords()
	 */
	@Override
    public Map<String, List<String>> getLanguageToKeywords() {
		return languageToKeywords;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setLanguageToKeywords(java.util.Map)
	 */
	@Override
    public void setLanguageToKeywords(Map<String, List<String>> languageToKeywords) {
		this.languageToKeywords = languageToKeywords;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getGoldenAnswers()
	 */
	@Override
    public Set<String> getGoldenAnswers() {
		return goldenAnswers;
	}

	/* (non-Javadoc)
	 * @see org.aksw.qa.commons.datastructure.IQuestion#setGoldenAnswers(java.util.Set)
	 */
	@Override
    public void setGoldenAnswers(Set<String> goldenAnswers) {
		this.goldenAnswers = goldenAnswers;
	}

}
