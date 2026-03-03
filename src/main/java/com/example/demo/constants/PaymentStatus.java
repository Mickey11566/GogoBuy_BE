package com.example.demo.constants;

public enum PaymentStatus {

	UNPAID("未付費"),
	PAID("已付費"),
	CONFIRMED("已確認"),
	SUBMITTED("待結單");

	private String message;

	PaymentStatus(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
