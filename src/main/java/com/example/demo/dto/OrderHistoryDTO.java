package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.constants.GroupbuyStatusEnum;
import com.example.demo.constants.PaymentStatus;
import com.example.demo.vo.OrderMenuVo;

public class OrderHistoryDTO {

	private String orderCode; // Use eventsId or a combination as code
	private int eventsId;
	private String eventName;
	private String storeName;
	private String hostName;
	private LocalDateTime createdAt;
	private GroupbuyStatusEnum eventStatus;
	private String statusLabel; // For frontend display
	private int totalAmount; // User's personal total (items sum)
	private int personFee; // Shipping fee share
	private String receiverName; // User's name
	private String phone; // User's phone
	private List<OrderMenuVo> items;
	private PaymentStatus paymentStatus;
	private String pickupStatus;
	private LocalDateTime pickupTime; // Actual pickup time
	private LocalDateTime eventPickupTime; // Scheduled pickup time
	private String pickLocation;
	private String personalMemo;

	// Getters and Setters
	public String getPersonalMemo() {
		return personalMemo;
	}

	public void setPersonalMemo(String personalMemo) {
		this.personalMemo = personalMemo;
	}

	public int getPersonFee() {
		return personFee;
	}

	public void setPersonFee(int personFee) {
		this.personFee = personFee;
	}

	public LocalDateTime getEventPickupTime() {
		return eventPickupTime;
	}

	public void setEventPickupTime(LocalDateTime eventPickupTime) {
		this.eventPickupTime = eventPickupTime;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public int getEventsId() {
		return eventsId;
	}

	public void setEventsId(int eventsId) {
		this.eventsId = eventsId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public GroupbuyStatusEnum getEventStatus() {
		return eventStatus;
	}

	public void setEventStatus(GroupbuyStatusEnum eventStatus) {
		this.eventStatus = eventStatus;
	}

	public String getStatusLabel() {
		return statusLabel;
	}

	public void setStatusLabel(String statusLabel) {
		this.statusLabel = statusLabel;
	}

	public int getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public void setReceiverName(String receiverName) {
		this.receiverName = receiverName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<OrderMenuVo> getItems() {
		return items;
	}

	public void setItems(List<OrderMenuVo> items) {
		this.items = items;
	}

	public PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPickupStatus() {
		return pickupStatus;
	}

	public void setPickupStatus(String pickupStatus) {
		this.pickupStatus = pickupStatus;
	}

	public LocalDateTime getPickupTime() {
		return pickupTime;
	}

	public void setPickupTime(LocalDateTime pickupTime) {
		this.pickupTime = pickupTime;
	}

	public String getPickLocation() {
		return pickLocation;
	}

	public void setPickLocation(String pickLocation) {
		this.pickLocation = pickLocation;
	}

}
