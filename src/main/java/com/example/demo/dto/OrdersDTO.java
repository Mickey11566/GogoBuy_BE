package com.example.demo.dto;

import java.util.List;

import com.example.demo.constants.PaymentStatus;
import com.example.demo.vo.OrderMenuVo;

public class OrdersDTO {

	private int eventsId;

	private String userId;

	private List<OrderMenuVo> menuList;

	private String personalMemo;

	private double weight;

	private PaymentStatus paymentStatus;

	public int getEventsId() {
		return eventsId;
	}

	public void setEventsId(int eventsId) {
		this.eventsId = eventsId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public List<OrderMenuVo> getMenuList() {
		return menuList;
	}

	public void setMenuList(List<OrderMenuVo> menuList) {
		this.menuList = menuList;
	}

	public String getPersonalMemo() {
		return personalMemo;
	}

	public void setPersonalMemo(String personalMemo) {
		this.personalMemo = personalMemo;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

}
