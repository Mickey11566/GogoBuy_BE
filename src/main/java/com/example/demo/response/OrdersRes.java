package com.example.demo.response;

import java.time.LocalDateTime;

import com.example.demo.constants.PickupStatusEnum;

public class OrdersRes extends BasicRes {

	private int id;

	private int eventsId;

	private String userId;

	private int menuId;
	
	private String menuName;
	
	private int basePrice;
	
	private int specPrice;

	private int quantity;

	private String selectedOption;

	private String personalMemo;

	private LocalDateTime orderTime;

	private PickupStatusEnum pickupStatus;

	private LocalDateTime pickupTime;

	private int subtotal;

	private double weight;



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



	public int getMenuId() {
		return menuId;
	}



	public void setMenuId(int menuId) {
		this.menuId = menuId;
	}



	public String getMenuName() {
		return menuName;
	}



	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}



	public int getBasePrice() {
		return basePrice;
	}



	public void setBasePrice(int basePrice) {
		this.basePrice = basePrice;
	}



	public int getQuantity() {
		return quantity;
	}



	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}



	public String getSelectedOption() {
		return selectedOption;
	}



	public void setSelectedOption(String selectedOption) {
		this.selectedOption = selectedOption;
	}



	public String getPersonalMemo() {
		return personalMemo;
	}



	public void setPersonalMemo(String personalMemo) {
		this.personalMemo = personalMemo;
	}



	public LocalDateTime getOrderTime() {
		return orderTime;
	}



	public void setOrderTime(LocalDateTime orderTime) {
		this.orderTime = orderTime;
	}



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



	public int getSubtotal() {
		return subtotal;
	}



	public void setSubtotal(int subtotal) {
		this.subtotal = subtotal;
	}



	public double getWeight() {
		return weight;
	}



	public void setWeight(double weight) {
		this.weight = weight;
	}


	public int getSpecPrice() {
		return specPrice;
	}



	public void setSpecPrice(int specPrice) {
		this.specPrice = specPrice;
	}



	public OrdersRes() {
		super();
	}



	public OrdersRes(int code, String message) {
		super(code, message);
	}



	public OrdersRes(int code, String message, int id, int eventsId, String userId, int menuId, String menuName,
			int basePrice, int specPrice, int quantity, String selectedOption, String personalMemo,
			LocalDateTime orderTime, PickupStatusEnum pickupStatus, LocalDateTime pickupTime, int subtotal,
			double weight) {
		super(code, message);
		this.id = id;
		this.eventsId = eventsId;
		this.userId = userId;
		this.menuId = menuId;
		this.menuName = menuName;
		this.basePrice = basePrice;
		this.specPrice = specPrice;
		this.quantity = quantity;
		this.selectedOption = selectedOption;
		this.personalMemo = personalMemo;
		this.orderTime = orderTime;
		this.pickupStatus = pickupStatus;
		this.pickupTime = pickupTime;
		this.subtotal = subtotal;
		this.weight = weight;
	}



	public OrdersRes(int code, String message, int subtotal) {
		super(code, message);
		this.subtotal = subtotal;
	}
}
