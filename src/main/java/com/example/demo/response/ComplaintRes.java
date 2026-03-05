package com.example.demo.response;

import com.example.demo.vo.ComplaintVo;

public class ComplaintRes extends BasicRes {
	private ComplaintVo complaintData;

	public ComplaintRes() {
		super();
	}
	public ComplaintRes(int code, String message) {
		super(code, message);
	}
	
	public ComplaintRes(int code, String message, ComplaintVo complaintData) {
		super(code, message);
		this.complaintData = complaintData;
	}
	public ComplaintVo getComplaintData() {
		return complaintData;
	}
	public void setComplaintData(ComplaintVo complaintData) {
		this.complaintData = complaintData;
	}
}
