package com.example.demo.constants;

public enum ResMessage {

	SUCCESS(200, "成功"), //
	PLEASE_LOGIN_FIRST(400, "請先登入"),
	LOGIN_ERROR(400, "登入失敗"),
	REGISTRATION_ERROR(400, "註冊失敗"),
	PASSWORD_ERROR(401, "密碼錯誤"),
	PENDING_ACTIVE(403, "帳戶尚未開通，請檢查電子信箱。"),
	BANNED(403, "帳戶因違規已被停權。"),
	SELF_SUSPENDED(403, "帳戶目前為停用狀態，請先申請復原"),
	PHONE_SIZE_ERROR(400,"電話長度錯誤"),
	VERIFICATION_ERROR(400, "驗證失敗，連結可能已過期"),
	NOTICE_ERROR(400,"公告新增失敗"),
	CATEGORY_ERROR(400,"大類型的類型錯誤(不存在)"),
	STORE_EXISTS(400,"店家(電話)已存在"),
	STORE_NULL_ERROR(400,"店家列表為空"),
	STORE_NOT_FOUND(404,"找不到該店家"),
	INPUT_IS_EMPTY(400,"請輸入搜尋關鍵字"),
	OUT_OF_TIMES_REMAINING(400, "超過許願次數"),
	WISH_ID_NOT_FOUND(400, "願望不存在"),
	WISH_IS_FINISHED(400, "願望已開團，不可再開團或跟願"),
	WISH_IS_COMPLETE(400, "願望已完成，不可刪除"),
    SAME_PASSWORD_ERROR(400, "新密碼與舊密碼相同"),
    EMAIL_ERROR(400, "信箱發送失敗"),
    OTP_EMPTY(400, "請先取得驗證碼"),    
    OTP_EXPIRED(400, "驗證碼已過期"),    
    OTP_ERROR(400, "驗證碼錯誤"),    
    EMAIL_EXITS(400, "該信箱已被使用"),
    EMAIL_SUCCESS(200, "認證信已寄出"),
    PHONE_ERROR(400, "請輸入手機號碼"),
	WISH_USER_CAN_NOT_FOLLOW(400, "許願者不可跟願"),
	WISH_DELETE_ERROR(400, "願望刪除失敗"),
	WISH_TYPE_ERROR(400, "願望類型無效"),
	HOST_ID_ERROR(400, "檢查團長ID錯誤"),
	HOST_ID_NOT_FOUND(404, "找不到團長ID"),
	STORES_ID_ERROR(400, "商店ID錯誤"),
	END_TIME_ERROR(400, "結束時間錯誤"),
	SPLIT_TYPE_ERROR(400, "拆帳錯誤"),
	SHIPPING_FEE_ERROR(400, "運費錯誤"),
	TOTALORDERAMOUNT_ERROR(400, "總金額錯誤"),
	TYPE_ERROR(400, "狀態錯誤"),
	EVENT_ERROR(400, "開團錯誤"),
	STORES_ID_NULL(400, "商店找不到錯誤"),
	MENU_NOT_FOUND(404, "找不到menu"),
	MENU_ITEM_NOT_FOUND(404, "找不到商品"),
	MENU_ITEM_ERROR(400, "推薦商品錯誤"),
	ORDER_ERROR(400, "找不到該訂單"),
	EVENTS_ID_ERROR(400, "所屬團ID錯誤"),
	MENU_ID_ERROR(400, "商品ID錯誤"),
	EVENTS_NOT_FOUND(404, "找不到所屬團"),
	MENULIST_NOT_FOUND(404, "找不到商品ID"),
	QUANTITY_FOUND(400, "數量錯誤"),
	PICKUP_STATUS_ERROR(400, "領取狀態錯誤"),
	PICKUP_TIME_ERROR(400, "領取時間錯誤"),
	EVENT_CLOSED(400, "此團購已截止，無法再下單"),
	UPDATE_ERROR(400,"更新失敗"),
	USER_NOT_FOUND(404, "找不到此用戶");
	
	private int code;

	private String message;

	private ResMessage(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
