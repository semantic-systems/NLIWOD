package org.aksw.mlqa.datastructure;

public class Run {

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

	private String name;
	private String submission;
	private Object fmeasure;

	public Run(String name, String submissionMax, double fMax) {
		this.name = name;
		this.submission = submissionMax;
		this.fmeasure = fMax;
	}

}