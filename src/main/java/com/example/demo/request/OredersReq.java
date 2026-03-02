package com.example.demo.request;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.constants.PickupStatusEnum;
import com.example.demo.vo.OrderMenuVo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OredersReq {

	private int id;
	@NotNull(message = "所屬團ID必填")
	private int eventsId;
	
    @NotBlank(message = "跟團者ID必填")
	private String userId;

	private String actingUserId;

	private List<OrderMenuVo> menuList;
	
	private String personalMemo;
	
	private LocalDateTime orderTime;
	
	private PickupStatusEnum pickupStatus;
	
	private LocalDateTime pickupTime;
	
	private int subtotal;
	
	private double weight;

	private boolean deleted;
	
	private String menuName;
	
	private int basePrice;
	
	private int specPrice;
	
	private int quantity;

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

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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

	public int getSpecPrice() {
		return specPrice;
	}

	public void setSpecPrice(int specPrice) {
		this.specPrice = specPrice;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getActingUserId() {
		return actingUserId;
	}

	public void setActingUserId(String actingUserId) {
		this.actingUserId = actingUserId;
	}
	
	
}
