package com.example.demo.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;

public class BlacklistReq {

	@NotBlank(message = "使用者ID 不能為空")
	@JsonProperty("userId")
	private String userId;

	@NotBlank(message = "封鎖對象 ID 不能為空")
	@JsonProperty("blockedUserId")
	private String blockedUserId;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getBlockedUserId() {
		return blockedUserId;
	}

	public void setBlockedUserId(String blockedUserId) {
		this.blockedUserId = blockedUserId;
	}

}
