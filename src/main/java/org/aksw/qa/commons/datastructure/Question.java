package org.aksw.qa.commons.datastructure;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.qa.commons.utils.CollectionUtils;

public class Question {

    public Integer id;
    public String answerType;
    public String pseudoSparqlQuery;
    public String sparqlQuery;
    public Boolean aggregation;
    public Boolean onlydbo;
    public Boolean outOfScope;
    public Boolean hybrid;
    public Boolean loadedAsASKQuery;
    public Map<String, String> languageToQuestion = CollectionUtils
	    .newLinkedHashMap();
    public Map<String, List<String>> languageToKeywords = CollectionUtils
	    .newLinkedHashMap();
    public Set<String> goldenAnswers = CollectionUtils.newHashSet();

    @Override
    public String toString() {
	return "Question [id=" + id + ", answerType=" + answerType
		+ ", aggregation=" + aggregation + ", onlydbo=" + onlydbo
		+ ", outOfScope=" + outOfScope + ", hybrid=" + hybrid
		+ ", pseudoSparqlQuery=" + pseudoSparqlQuery + ", sparqlQuery="
		+ sparqlQuery + ", languageToQuestion=" + languageToQuestion
		+ ", languageToKeywords=" + languageToKeywords
		+ ", goldenAnswers=" + goldenAnswers + "]";
    }

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

}
