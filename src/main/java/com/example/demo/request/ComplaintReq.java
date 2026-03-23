package com.example.demo.request;

import com.example.demo.constants.ValidationMsg;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ComplaintReq {
	@NotBlank(message=ValidationMsg.USER_ID_EMPTY)
	private String complaintUuid;
	@NotBlank(message=ValidationMsg.USER_ID_EMPTY)
	private String respondentUuid;
	@NotBlank(message=ValidationMsg.REASON_EMPTY)
	private String reason;
	@NotNull(message=ValidationMsg.EVENT_ID_EMPTY)
	private int eventId;
	
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
}
