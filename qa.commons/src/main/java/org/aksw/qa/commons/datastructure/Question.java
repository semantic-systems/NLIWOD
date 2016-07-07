package org.aksw.qa.commons.datastructure;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.utils.CollectionUtils;

public class Question implements IQuestion {

	private Integer id;
	private String answerType;
	private String pseudoSparqlQuery;
	private Map<String, String> sparqlQuery;
	private Boolean aggregation;
	private Boolean onlydbo;
	private Boolean outOfScope;
	private Boolean hybrid;
	private Map<String, String> languageToQuestion;
	private Map<String, List<String>> languageToKeywords;
	private Map<String, Set<String>> goldenAnswers;

	public Question() {
		HashSet<String> ga = CollectionUtils.newHashSet();
		goldenAnswers = new HashMap<String, Set<String>>();
		goldenAnswers.put("en", ga);

		sparqlQuery = new HashMap<String, String>();

		languageToQuestion = CollectionUtils.newLinkedHashMap();
		languageToKeywords = CollectionUtils.newLinkedHashMap();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#toString()
	 */
	@Override
	public String toString() {
		return "Question [id=" + id + ", answerType=" + answerType + ", aggregation=" + aggregation + ", onlydbo=" + onlydbo + ", outOfScope=" + outOfScope + ", hybrid=" + hybrid
		        + ", pseudoSparqlQuery=" + pseudoSparqlQuery + ", sparqlQuery=" + sparqlQuery + ", languageToQuestion=" + languageToQuestion + ", languageToKeywords=" + languageToKeywords
		        + ", goldenAnswers=" + goldenAnswers + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setValue(java.lang.String,
	 * java.lang.String)
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


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getAnswerType()
	 */
	@Override
	public String getAnswerType() {
		return answerType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setAnswerType(java.lang.String
	 * )
	 */
	@Override
	public void setAnswerType(String answerType) {
		this.answerType = answerType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getPseudoSparqlQuery()
	 */
	@Override
	public String getPseudoSparqlQuery() {
		return pseudoSparqlQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setPseudoSparqlQuery(java
	 * .lang.String)
	 */
	@Override
	public void setPseudoSparqlQuery(String pseudoSparqlQuery) {
		this.pseudoSparqlQuery = pseudoSparqlQuery;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getSparqlQuery()
	 */
	@Override
	public String getSparqlQuery() {
		return sparqlQuery.get("en");
	}

	public String getSparqlQuery(String lang) {
		return sparqlQuery.get(lang);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setSparqlQuery(java.lang.
	 * String)
	 */
	@Override
	public void setSparqlQuery(String sparqlQuery) {
		this.sparqlQuery.put("en", sparqlQuery);
	}

	public void setSparqlQuery(String lang, String sparqlQuery) {
		this.sparqlQuery.put(lang, sparqlQuery);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getAggregation()
	 */
	@Override
	public Boolean getAggregation() {
		return aggregation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setAggregation(java.lang.
	 * Boolean)
	 */
	@Override
	public void setAggregation(Boolean aggregation) {
		this.aggregation = aggregation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getOnlydbo()
	 */
	@Override
	public Boolean getOnlydbo() {
		return onlydbo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setOnlydbo(java.lang.Boolean)
	 */
	@Override
	public void setOnlydbo(Boolean onlydbo) {
		this.onlydbo = onlydbo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getOutOfScope()
	 */
	@Override
	public Boolean getOutOfScope() {
		return outOfScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setOutOfScope(java.lang.Boolean
	 * )
	 */
	@Override
	public void setOutOfScope(Boolean outOfScope) {
		this.outOfScope = outOfScope;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getHybrid()
	 */
	@Override
	public Boolean getHybrid() {
		return hybrid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setHybrid(java.lang.Boolean)
	 */
	@Override
	public void setHybrid(Boolean hybrid) {
		this.hybrid = hybrid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getLanguageToQuestion()
	 */
	@Override
	public Map<String, String> getLanguageToQuestion() {
		return languageToQuestion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setLanguageToQuestion(java
	 * .util.Map)
	 */
	@Override
	public void setLanguageToQuestion(Map<String, String> languageToQuestion) {
		this.languageToQuestion = languageToQuestion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getLanguageToKeywords()
	 */
	@Override
	public Map<String, List<String>> getLanguageToKeywords() {
		return languageToKeywords;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setLanguageToKeywords(java
	 * .util.Map)
	 */
	@Override
	public void setLanguageToKeywords(Map<String, List<String>> languageToKeywords) {
		this.languageToKeywords = languageToKeywords;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.aksw.qa.commons.datastructure.IQuestion#getGoldenAnswers()
	 */
	@Override
	public Set<String> getGoldenAnswers() {
		return goldenAnswers.get("en");
	}

	public Set<String> getGoldenAnswers(String lang) {
		return goldenAnswers.get(lang);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.aksw.qa.commons.datastructure.IQuestion#setGoldenAnswers(java.util
	 * .Set)
	 */
	@Override
	public void setGoldenAnswers(Set<String> goldenAnswers) {
		this.goldenAnswers.put("en", goldenAnswers);
	}

	public void setGoldenAnswers(String lang, Set<String> goldenAnswers) {
		this.goldenAnswers.put(lang, goldenAnswers);
	}

	@Override
    public String getId() {
	    // TODO Auto-generated method stub
	    return null;
    }

	@Override
    public void setId(String id) {
	    // TODO Auto-generated method stub
	    
    }

}
