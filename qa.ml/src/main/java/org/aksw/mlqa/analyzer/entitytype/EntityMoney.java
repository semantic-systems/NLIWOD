package org.aksw.mlqa.analyzer.entitytype;

import java.util.ArrayList;

import org.aksw.mlqa.analyzer.IAnalyzer;

import weka.core.Attribute;

/***
 * Analyzes if there is an entity of type Money in the question.
 * @author Lukas
 *
 */
public class EntityMoney extends Entity implements IAnalyzer {
	private Attribute attribute = null;
	
	public EntityMoney() {
		ArrayList<String> fvWekaMoney = new ArrayList<String>();
		fvWekaMoney.add("Money");
		fvWekaMoney.add("NoMoney");
		attribute = new Attribute("Money", fvWekaMoney);
	}

	@Override
	public Object analyze(String q) {
		return recognizeEntity("Money", q);
	}

	@Override
	public Attribute getAttribute() {
		return attribute;
	}
}
