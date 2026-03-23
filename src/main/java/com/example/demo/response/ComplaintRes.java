package com.example.demo.response;

import com.example.demo.vo.ComplaintVo;

public class ComplaintRes extends BasicRes {
	private ComplaintVo vo;

	public ComplaintRes() {
		super();
	}

	public ComplaintRes(int code, String message) {
		super(code, message);
	}

	public ComplaintRes(int code, String message, ComplaintVo vo) {
		super(code, message);
		this.vo = vo;
	}

	public ComplaintVo getVo() {
		return vo;
	}

	public void setVo(ComplaintVo vo) {
		this.vo = vo;
	}
}
