package com.example.demo.constants;

public enum UserStatusEnum {
	PENDING_ACTIVE("待開通"), //
	ACTIVE("活躍中"), //
	BANNED("停權中"), //
	SELF_SUSPENDED("停用中");

	private String status;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	private UserStatusEnum(String status) {
		this.status = status;
	}

}
