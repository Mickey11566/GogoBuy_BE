package com.example.demo.constants;

public class ValidationMsg {

//	加上 final 是因為使用在@Validation 中的 message 的限制
//	加上 static 讓 ACCOUNT_ERROR可以直接透過 ValidationMsg class呼叫
	public static final String STORE_NAME_EMPTY = " 商店名是空的 ";
	public static final String NAME_ERROR = " 名字名是空的 ";
	public static final String PHONE_EMPTY = " 電話是空的 ";
	public static final String PHONE_ERROR = " 電話格式錯誤";
	public static final String EMAIL_ERROR = " Email 錯誤 ";
	public static final String PASSWORD_ERROR = " 密碼錯誤 ";
	public static final String ADDRESS_EMPTY = " 地址是空的";
	public static final String CATEGORY_EMPTY = " 不知道是雜貨還是餐飲 ";
	public static final String TYPE_EMPTY = " 商家類型是空的 ";
	public static final String FEE_DESCRIPTION_EMPTY = " 運費描述是空的 ";
	
	public static final String USER_ID_ERROR = " User ID 錯誤";
	public static final String USER_ID_EMPTY = " User ID 為空";
	public static final String TITLE_EMPTY = " 許願標題為空";
	public static final String ANONYMOUS_EMPTY = " 匿名為空";
	public static final String LOCATION_EMPTY = " 取貨點為空";
	
	public static final String REASON_EMPTY = "理由為空";
	public static final String EVENT_ID_EMPTY = "Event ID 為空";

}
