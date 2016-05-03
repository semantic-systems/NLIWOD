package org.aksw.qa.systems;

import java.util.HashMap;
import java.util.Map;

import org.aksw.qa.commons.datastructure.IQuestion;
import org.aksw.qa.commons.datastructure.Question;
import org.aksw.qa.util.ResponseToStringParser;

public abstract class ASystem {
	
	protected ResponseToStringParser responseparser = new ResponseToStringParser();
	
    public IQuestion search(String question) {
        IQuestion iQuestion = new Question();
        Map<String, String> langToQuestion = new HashMap<String, String>();
        langToQuestion.put("en", question);
        iQuestion.setLanguageToQuestion(langToQuestion);
        search(iQuestion);
        return iQuestion;
    }

    public abstract void search(IQuestion question);

    public abstract String name();

}
