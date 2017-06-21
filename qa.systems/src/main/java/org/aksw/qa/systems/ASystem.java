package org.aksw.qa.systems;

import java.util.HashMap;
import java.util.Map;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.util.ResponseToStringParser;

public abstract class ASystem {
	
	protected ResponseToStringParser responseparser = new ResponseToStringParser();
	protected int timeout=0;
	protected boolean setLangPar=false;
	
	public void setSocketTimeOutMs(int timeout){
		this.timeout=timeout;
	}
	
    public IQuestion search(String question, String language) throws Exception{
    	return search(question, language, false);
    }

	public IQuestion search(String question, String language, boolean setLangPar) throws Exception{
        if(language == null){
        	language="en";
        }
        this.setLangPar=setLangPar;
    	IQuestion iQuestion = new Question();
        Map<String, String> langToQuestion = new HashMap<String, String>();
        langToQuestion.put(language, question);
        iQuestion.setLanguageToQuestion(langToQuestion);
        search(iQuestion, language);
        return iQuestion;
    }

    public abstract void search(IQuestion question, String language) throws Exception;

    public abstract String name();

	public void setSetLangPar(boolean setLangPar) {
		this.setLangPar = setLangPar;
	}

}
