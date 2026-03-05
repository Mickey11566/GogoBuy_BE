package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="complaint")
public class Complaint {
	@Id
	@Column(name="id")
	private int id;
	@Column(name="complaint_uuid")
	private String complaintUuid;
	@Column(name="respondent_uuid")
	private String respondentUuid;
	@Column(name="reason")
	private String reason;
	@Column(name="event_id")
	private int eventId;
	@Column(name="is_completed")
	private boolean completed;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getComplaintUuid() {
		return complaintUuid;
	}
	public void setComplaintUuid(String complaintUuid) {
		this.complaintUuid = complaintUuid;
	}
	public String getRespondentUuid() {
		return respondentUuid;
	}
	public void setRespondentUuid(String respondentUuid) {
		this.respondentUuid = respondentUuid;
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
