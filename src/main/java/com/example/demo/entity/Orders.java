package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.constants.PickupStatusEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Orders {

	@Id
	//必須告訴 Hibernate 使用資料庫的「自動遞增」機制
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "events_id")
	private int eventsId;
	
	@Column(name = "user_id")
	private String userId;
	
	@Column(name = "menu_id")
	private int menuId;
	
	@Column(name = "quantity")
	private int quantity;
	
	@Column(name = "spec_name")
	private String specName;
	
	@Column(name = "selected_option")
	private String selectedOption;
	
	@Column(name = "order_time")
	private LocalDateTime orderTime;
	
	@Column(name = "pickup_status")
	@Enumerated(EnumType.STRING) 
	private PickupStatusEnum pickupStatus;
	
	@Column(name = "pickup_time")
	private LocalDateTime pickupTime;
	
	@Column(name = "subtotal")
	private int subtotal;
	
	@Column(name = "personal_memo")
	private String personalMemo;
	
	@Column(name = "weight")
	private double weight;
	
	@Column(name = "is_deleted")
	private boolean deleted;
	
	@Column(name = "menu_name")
	private String menuName;
	
	@Column(name = "base_price")
	private int basePrice;
	
	@Column(name = "spec_price")
	private int specPrice;

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

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getSpecName() {
		return specName;
	}

	public void setSpecName(String specName) {
		this.specName = specName;
	}

	public String getSelectedOption() {
		return selectedOption;
	}

	public void setSelectedOption(String selectedOption) {
		this.selectedOption = selectedOption;
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

	public String getPersonalMemo() {
		return personalMemo;
	}

	public void setPersonalMemo(String personalMemo) {
		this.personalMemo = personalMemo;
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


	
}
