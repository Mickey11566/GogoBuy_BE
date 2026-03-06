package com.example.demo.response;

import java.util.List;

import com.example.demo.vo.ComplaintVo;

public class ComplaintRes extends BasicRes {
	private List<ComplaintVo> vo;

	public ComplaintRes() {
		super();
	}
	public ComplaintRes(int code, String message) {
		super(code, message);
	}
	
	public ComplaintRes(int code, String message, List<ComplaintVo> vo) {
		super(code, message);
		this.vo = vo;
	}
	public List<ComplaintVo> getVo() {
		return vo;
	}
	public void setVo(List<ComplaintVo> vo) {
		this.vo = vo;
	}
}
