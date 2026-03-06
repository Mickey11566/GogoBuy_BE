package com.example.demo.constants;

public enum GroupbuyStatusEnum {
	OPEN("進行中"),  
	FINISHED("已結束"), 
	CANCELLED("未成團");
	
	private String message;

	GroupbuyStatusEnum(String string) {
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
}
