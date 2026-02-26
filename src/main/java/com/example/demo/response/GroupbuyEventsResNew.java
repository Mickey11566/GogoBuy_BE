package com.example.demo.response;

import java.time.LocalDateTime;

import com.example.demo.constants.GroupbuyStatusEnum;
import com.example.demo.constants.SplitTypeEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_DEFAULT)
public class GroupbuyEventsResNew extends BasicRes{

	private int id;
	
	private String hostId;

	private int storesId;

	private String eventName;

	private GroupbuyStatusEnum status;

	private LocalDateTime endTime;

	private int totalOrderAmount;

	private int shippingFee;

	private SplitTypeEnum splitType;

	private String announcement;

	private String type;

	private String tempMenuList;

	private String recommendList;

	private String recommendDescription;
	
	private int limitation;

	private boolean deleted;
	
	private LocalDateTime pickupTime;
	
	private String pickLocation;

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

	public GroupbuyEventsResNew() {
		super();
		// TODO Auto-generated constructor stub
	}

	public GroupbuyEventsResNew(int code, String message) {
		super(code, message);
		// TODO Auto-generated constructor stub
	}

	

	public GroupbuyEventsResNew(int code, String message, int id, String hostId, int storesId, String eventName,
			GroupbuyStatusEnum status, LocalDateTime endTime, int totalOrderAmount, int shippingFee,
			SplitTypeEnum splitType, String announcement, String type, String tempMenuList, String recommendList,
			String recommendDescription, int limitation, boolean deleted, LocalDateTime pickupTime,
			String pickLocation) {
		super(code, message);
		this.id = id;
		this.hostId = hostId;
		this.storesId = storesId;
		this.eventName = eventName;
		this.status = status;
		this.endTime = endTime;
		this.totalOrderAmount = totalOrderAmount;
		this.shippingFee = shippingFee;
		this.splitType = splitType;
		this.announcement = announcement;
		this.type = type;
		this.tempMenuList = tempMenuList;
		this.recommendList = recommendList;
		this.recommendDescription = recommendDescription;
		this.limitation = limitation;
		this.deleted = deleted;
		this.pickupTime = pickupTime;
		this.pickLocation = pickLocation;
	}

	public GroupbuyEventsResNew(int code, String message, int id) {
		super(code, message);
		this.id = id;
	}
	
	
}
