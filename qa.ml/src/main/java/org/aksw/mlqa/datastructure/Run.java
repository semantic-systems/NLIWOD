package org.aksw.mlqa.datastructure;

import java.util.Map;

public class Run {

	private String name;
	private String submission;
	private Object fmeasure;
	private Map<String,Double> map;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubmission() {
		return submission;
	}

	public void setSubmission(String submission) {
		this.submission = submission;
	}

	public Object getFmeasure() {
		return fmeasure;
	}

	public void setFmeasure(Object fmeasure) {
		this.fmeasure = fmeasure;
	}

	public Run(String name) {
		this.name = name;
	}


	public Map<String,Double> getMap() {
	    return map;
    }

	public void setMap(Map<String,Double> map) {
		// stores the fmeasures for each question
	    this.map = map;
    }

}