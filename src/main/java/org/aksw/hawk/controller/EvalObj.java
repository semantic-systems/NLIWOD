package org.aksw.hawk.controller;

public class EvalObj {
	private String comment;
	private double fmax;
	private double pmax;
	private double rmax;
	private String question;
	private int id;

	public EvalObj(int id, String question, double fmax, double pmax, double rmax, String comment) {
		this.id = id;
		this.question = question;
		this.fmax = fmax;
		this.rmax = rmax;
		this.pmax = pmax;
		this.comment = comment;
	}

	public int getId() {
		return id;
	}

	public String getComment() {
		return comment;
	}

	public double getFmax() {
		return fmax;
	}

	public double getPmax() {
		return pmax;
	}

	public double getRmax() {
		return rmax;
	}

	public String getQuestion() {
		return question;
	}
}
