package com.example.demo.vo;

public class ComplaintVo {
	private int id;
	private String complaintUserName;
	private String respondentUserName;
	private String reason;
	private int eventId;
	private boolean completed;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getComplaintUserName() {
		return complaintUserName;
	}

	public void setComplaintUserName(String complaintUserName) {
		this.complaintUserName = complaintUserName;
	}

	public String getRespondentUserName() {
		return respondentUserName;
	}

	public void setRespondentUserName(String respondentUserName) {
		this.respondentUserName = respondentUserName;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}
}
