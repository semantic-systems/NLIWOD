package org.aksw.mlqa.analyzer.questiontype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

/**
 * Analyzes what answerset type is expected. RESOURCE, BOOLEAN, LIST or NUMBER.
 * @author Lukas
 *
 */
public class QuestionTypeAnalyzer implements IAnalyzer {
	
	@Override
	public Object analyze(String q) {
		if (isNumberQuestion(q)) {
			return QuestionTypeFeature.NUMBER.name();
		} else if(isListQuestion(q)) {
			return QuestionTypeFeature.LIST.name();
		} else if(isASKQuestion(q)) {
			return QuestionTypeFeature.BOOLEAN.name();
		} else {
			return QuestionTypeFeature.RESOURCE.name();
		}
	}

	@Override
	public Attribute getAttribute() {
		return QuestionTypeFeature.attribute;
	}

	// Enumeration values
	enum QuestionTypeFeature {
		LIST, NUMBER, BOOLEAN, RESOURCE;
		private static Attribute attribute = null;
		static {
			ArrayList<String> attributeValues = new ArrayList<String>();
			for (QuestionTypeFeature qtf : QuestionTypeFeature.values()) {
				attributeValues.add(qtf.name());				
			}
			attribute = new Attribute("QuestionType", attributeValues);
		};
	}
	
	/***
	 * Returns true if the given question is a ask question.
	 * @param question
	 * @return true if ask query false otherwise
	 */
	public static Boolean isASKQuestion(String question) {
		return question.startsWith("Are ") || question.startsWith("Did ") || question.startsWith("Do ") || question.startsWith("Does ") || question.startsWith("Is ") || question.startsWith("Was ");
	}
	
	/**
	 * Returns true if it is a list question.
	 * @param question
	 * @return true if list question false otherwise
	 */
	public Boolean isListQuestion(String question) {
		return question.startsWith("List ") || question.startsWith("Give ") || question.startsWith("Show ");
	}
	
	/**
	 * Returns true if it is a number question. 
	 * @param question
	 * @return true if number question false otherwise
	 */
	public Boolean isNumberQuestion(String question) {
		return question.startsWith("How ");
	}
}
