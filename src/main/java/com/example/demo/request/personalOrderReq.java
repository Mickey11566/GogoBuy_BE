package com.example.demo.request;

import java.time.LocalDateTime;

import com.example.demo.constants.PaymentStatus;
import com.example.demo.constants.PickupStatusEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class personalOrderReq {

	private int id;
	@NotNull(message = "團ID必填")
	private int eventsId;

	@NotBlank(message = "跟團者ID必填")
	private String userId;

	private double totalWeight;

	@NotNull(message = "個人運費必填")
	private int personFee;

	@NotNull(message = "該單總額必填")
	private int totalSum;

	private PaymentStatus paymentStatus;

	private LocalDateTime paymentTime;

	private PickupStatusEnum pickupStatus;

	private LocalDateTime pickupTime;

	public PickupStatusEnum getPickupStatus() {
		return pickupStatus;
	}

	public void setPickupStatus(PickupStatusEnum pickupStatus) {
		this.pickupStatus = pickupStatus;
	}

	public LocalDateTime getPickupTime() {
		return pickupTime;
	}

	public void setPickupTime(LocalDateTime pickupTime) {
		this.pickupTime = pickupTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

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

	public double getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(double totalWeight) {
		this.totalWeight = totalWeight;
	}

	public int getPersonFee() {
		return personFee;
	}

	public void setPersonFee(int personFee) {
		this.personFee = personFee;
	}

	public int getTotalSum() {
		return totalSum;
	}

	public void setTotalSum(int totalSum) {
		this.totalSum = totalSum;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public LocalDateTime getPaymentTime() {
		return paymentTime;
	}

	public void setPaymentTime(LocalDateTime paymentTime) {
		this.paymentTime = paymentTime;
	}

}
