package com.example.demo.entity;

import java.time.LocalDateTime;

import com.example.demo.constants.GroupbuyStatusEnum;
import com.example.demo.constants.SplitTypeEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "groupbuy_events")
public class GroupbuyEvents {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;

	@Column(name = "host_id")
	private String hostId;

	@Column(name = "stores_id")
	private int storesId;

	@Column(name = "event_name")
	private String eventName;

	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private GroupbuyStatusEnum status;

	@Column(name = "end_time")
	private LocalDateTime endTime;

	@Column(name = "total_order_amount")
	private int totalOrderAmount;

	@Column(name = "shipping_fee")
	private int shippingFee;

	@Column(name = "split_type")
	@Enumerated(EnumType.STRING)
	private SplitTypeEnum splitType;

	@Column(name = "announcement")
	private String announcement;

	@Column(name = "type")
	private String type;

	@Column(name = "temp_menu")
	private String tempMenuList;

	@Column(name = "recommend")
	private String recommendList;

	@Column(name = "recommend_description")
	private String recommendDescription;

	@Column(name = "limitation")
	private int limitation;

	@Column(name = "is_deleted")
	private boolean deleted;
	
	@Column(name = "pickup_time")
	private LocalDateTime pickupTime;
	
	@Column(name = "pick_location")
	private String pickLocation;

	@Transient // 代表此欄位不屬於資料庫表，僅供程式內部使用
	private String nickname;

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public int getStoresId() {
		return storesId;
	}

	public void setStoresId(int storesId) {
		this.storesId = storesId;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public GroupbuyStatusEnum getStatus() {
		return status;
	}

	public void setStatus(GroupbuyStatusEnum status) {
		this.status = status;
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

	public int getShippingFee() {
		return shippingFee;
	}

	public void setShippingFee(int shippingFee) {
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public int getLimitation() {
		return limitation;
	}

	public void setLimitation(int limitation) {
		this.limitation = limitation;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
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
