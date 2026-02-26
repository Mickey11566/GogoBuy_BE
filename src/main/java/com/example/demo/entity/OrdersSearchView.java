package com.example.demo.entity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.demo.constants.PickupStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


@Entity
@Table(name = "orders_search_view")
public class OrdersSearchView {

	@Id
	@Column(name = "order_id")
	private int orderId;
	
	@Column(name = "event_id")
	private int eventId;
	
	@Column(name = "user_id")
	private String userId;
	
	@Column(name = "menu_id")
	private int menuId;
	
	@Column(name = "quantity")
	private int quantity;
	
	@Column(name = "selected_option")
	@JsonIgnore // 加上這行，JSON 就不會出現這個字串欄位了
	private String selectedOption;
	
	@Transient // 關鍵：這不會儲存在資料庫
    private List<Map<String, Object>> selectedOptionList;
	
	@Column(name = "personal_memo")
	private String personalMemo;
	
	@Column(name = "order_time")
	private LocalDateTime orderTime;
	
	@Column(name = "pickup_status")
	@Enumerated(EnumType.STRING) 
	private PickupStatusEnum pickupStatus;
	
	@Column(name = "pickup_time")
	private LocalDateTime pickupTime;
	
	@Column(name = "subtotal")
	private int subtotal;
	
	@Column(name = "weight")
	private double weight;
	
	@Column(name = "is_deleted")
	private boolean deleted;
	
	@Column(name = "menu_name")
	private String menuName;
	
	@Column(name = "host_id")
	private String hostId;
	
	@Column(name = "user_nickname")
	private String hostNickname;	

	public List<Map<String, Object>> getSelectedOptionList() {
		return selectedOptionList;
	}

	public void setSelectedOptionList(List<Map<String, Object>> selectedOptionList) {
		this.selectedOptionList = selectedOptionList;
	}

	public int getOrderId() {
		return orderId;
	}

	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
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

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getHostNickname() {
		return hostNickname;
	}

	public void setHostNickname(String hostNickname) {
		this.hostNickname = hostNickname;
	}
	
	
	
}
