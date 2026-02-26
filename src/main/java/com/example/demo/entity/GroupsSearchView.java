package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.constants.GroupbuyStatusEnum;
import com.example.demo.constants.SplitTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "groups_search_view")
public class GroupsSearchView {
	
	@Id
	@Column(name = "event_id")
	private int eventId;
	
	@Column(name = "event_name")
	private String eventName;
	
	@Column(name = "host_nickname")
	private String hostNickname;
	
	@Column(name = "store_id")
	private int storeId;
	
	@Column(name = "event_status")
	@Enumerated(EnumType.STRING)  
	private GroupbuyStatusEnum eventStatus;
	
	@Column(name = "end_time")
	private LocalDateTime endTime;
	
	@Column(name = "total_order_amount")
	private int totalOrderAmount;
	
	@Column(name = "shipping_fee")
	private Integer shippingFee;
	
	@Column(name = "split_type")
	@Enumerated(EnumType.STRING)  
	private SplitTypeEnum splitType;
	
	@Column(name = "announcement")
	private String announcement;
	
	@Column(name = "event_type")
	private String eventType;
	
	@Column(name = "temp_menu")
	private String tempMenuList;
	
	@Column(name = "recommend")
	private String recommendList;
	
	@Column(name = "recommend_description")
	private String recommendDescription;
	
	@Column(name = "limitation")
	private Integer limitation;
	
	@Column(name = "store_name")
	private String storeName;
	
	@Column(name = "store_category")
	private String storeCategory;
	
	@Column(name = "store_image")
	private String storeImage;
	
	@Column(name = "host_id")
	private String hostId;
	
	@Column(name = "host_avatar")
	private String hostAvatar;
	
	@Column(name = "is_deleted")
	private boolean deleted;
	
	@Column(name = "pickup_time")
	private LocalDateTime pickupTime;
	
	@Column(name = "pick_location")
	private String pickLocation;

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public int getStoreId() {
		return storeId;
	}

	public void setStoreId(int storeId) {
		this.storeId = storeId;
	}

	public GroupbuyStatusEnum getEventStatus() {
		return eventStatus;
	}

	public void setEventStatus(GroupbuyStatusEnum eventStatus) {
		this.eventStatus = eventStatus;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public int getTotalOrderAmount() {
		return totalOrderAmount;
	}

	public void setTotalOrderAmount(int totalOrderAmount) {
		this.totalOrderAmount = totalOrderAmount;
	}

	public Integer getShippingFee() {
		return shippingFee;
	}

	public void setShippingFee(Integer shippingFee) {
		this.shippingFee = shippingFee;
	}

	public SplitTypeEnum getSplitType() {
		return splitType;
	}

	public void setSplitType(SplitTypeEnum splitType) {
		this.splitType = splitType;
	}

	public String getAnnouncement() {
		return announcement;
	}

	public void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getTempMenuList() {
		return tempMenuList;
	}

	public void setTempMenuList(String tempMenuList) {
		this.tempMenuList = tempMenuList;
	}

	public String getRecommendList() {
		return recommendList;
	}

	public void setRecommendList(String recommendList) {
		this.recommendList = recommendList;
	}

	public String getRecommendDescription() {
		return recommendDescription;
	}

	public void setRecommendDescription(String recommendDescription) {
		this.recommendDescription = recommendDescription;
	}

	public Integer getLimitation() {
		return limitation;
	}

	public void setLimitation(Integer limitation) {
		this.limitation = limitation;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getStoreCategory() {
		return storeCategory;
	}

	public void setStoreCategory(String storeCategory) {
		this.storeCategory = storeCategory;
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

	public String getHostAvatar() {
		return hostAvatar;
	}

	public void setHostAvatar(String hostAvatar) {
		this.hostAvatar = hostAvatar;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public String getStoreImage() {
		return storeImage;
	}

	public void setStoreImage(String storeImage) {
		this.storeImage = storeImage;
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
