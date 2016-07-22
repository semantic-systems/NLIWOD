package org.aksw.hawk.controller;

import org.aksw.hawk.datastructures.Answer;

public class EvalObj {
	private String comment;
	private double fmax;
	private double pmax;
	private double rmax;
	private String question;
	private String id;
	private Answer answer;

	public EvalObj(String id, String question, double fmax, double pmax, double rmax, String comment, Answer answer) {
		this.id = id;
		this.question = question;
		this.fmax = fmax;
		this.rmax = rmax;
		this.pmax = pmax;
		this.comment = comment;
		this.answer = answer;
	}

	public Answer getAnswer() {
		return answer;
	}

	@Override
	public String toString() {
		return "EvalObj [comment=" + comment + ", fmax=" + fmax + ", pmax=" + pmax + ", rmax=" + rmax + ", question=" + question + ", id=" + id + "]";
	}

	public String getId() {
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
