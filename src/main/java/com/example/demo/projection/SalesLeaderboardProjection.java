package com.example.demo.projection;

public interface SalesLeaderboardProjection {
	Integer getMenuId();
	Integer getStoreId();
    String getProductName(); // 對應 menu 表的名稱
    String getProductImage(); // 對應 menu 表的圖片
    Integer getSalesVolume();
    String getStatsType();
    String getStoreName();
}
