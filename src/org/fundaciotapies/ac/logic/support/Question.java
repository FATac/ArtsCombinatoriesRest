package org.fundaciotapies.ac.logic.support;

public class Question {
	private String questionId;
	private String[] answerList;
	private String color = null;
	
	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}
	public String getQuestionId() {
		return questionId;
	}
	public void setAnswerList(String[] answerList) {
		this.answerList = answerList;
	}
	public String[] getAnswerList() {
		return answerList;
	}
	public void setColor(String color) {
		this.color = color;
	}
	public String getColor() {
		return color;
	}
}