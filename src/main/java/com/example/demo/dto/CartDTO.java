package com.example.demo.dto;

import java.util.List;

import com.example.demo.constants.GroupbuyStatusEnum;
import com.example.demo.entity.Orders;

public class CartDTO {

	private int eventsId;
	private String eventName;
	private String storeName;
	private String storeLogo;
	private String hostLogo;
	private int totalAmount;
	private int totalQuantity;
	private String latestOrderTime;
	private GroupbuyStatusEnum status; // OPEN, LOCKED, FINISHED
	private boolean canModify; // 根據 status 判斷是否還能修改
	private String pickLocation;
	private String pickupTime;

	// 統計資訊 (管理用)
	private int unpaidCount;
	private int unpickedCount;

	// 這一團裡面點的所有商品細項
	private List<Orders> items;

	public int getUnpaidCount() {
		return unpaidCount;
	}

	public void setUnpaidCount(int unpaidCount) {
		this.unpaidCount = unpaidCount;
	}

	public int getUnpickedCount() {
		return unpickedCount;
	}

	public void setUnpickedCount(int unpickedCount) {
		this.unpickedCount = unpickedCount;
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

	public String getStoreLogo() {
		return storeLogo;
	}

	public void setStoreLogo(String storeLogo) {
		this.storeLogo = storeLogo;
	}

	public String getHostLogo() {
		return hostLogo;
	}

	public void setHostLogo(String hostLogo) {
		this.hostLogo = hostLogo;
	}

	public int getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(int totalAmount) {
		this.totalAmount = totalAmount;
	}

	public int getTotalQuantity() {
		return totalQuantity;
	}

	public void setTotalQuantity(int totalQuantity) {
		this.totalQuantity = totalQuantity;
	}

	public String getLatestOrderTime() {
		return latestOrderTime;
	}

	public void setLatestOrderTime(String latestOrderTime) {
		this.latestOrderTime = latestOrderTime;
	}

	public GroupbuyStatusEnum getStatus() {
		return status;
	}

	public void setStatus(GroupbuyStatusEnum status) {
		this.status = status;
	}

	public boolean isCanModify() {
		return canModify;
	}

	public void setCanModify(boolean canModify) {
		this.canModify = canModify;
	}

	public List<Orders> getItems() {
		return items;
	}

	public void setItems(List<Orders> items) {
		this.items = items;
	}

	public String getPickLocation() {
		return pickLocation;
	}

	public void setPickLocation(String pickLocation) {
		this.pickLocation = pickLocation;
	}

	public String getPickupTime() {
		return pickupTime;
	}

	public void setPickupTime(String pickupTime) {
		this.pickupTime = pickupTime;
	}

}
