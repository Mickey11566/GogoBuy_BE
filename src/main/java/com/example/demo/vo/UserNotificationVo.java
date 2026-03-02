package com.example.demo.vo;

public class UserNotificationVo {

	private int id;
	private String userId;
	private int notifId;
	private boolean checked;
	private boolean deleted;
	private String email;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public int getNotifId() {
		return notifId;
	}
	public void setNotifId(int notifId) {
		this.notifId = notifId;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public UserNotificationVo() {
		super();
	}
	public UserNotificationVo(int id, String userId, int notifId, boolean checked, boolean deleted, String email) {
		super();
		this.id = id;
		this.userId = userId;
		this.notifId = notifId;
		this.checked = checked;
		this.deleted = deleted;
		this.email = email;
	}
	
}
