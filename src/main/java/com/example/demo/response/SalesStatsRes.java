package com.example.demo.response;

import java.util.List;

import com.example.demo.entity.SalesStats;
import com.example.demo.projection.SalesLeaderboardProjection;

public class SalesStatsRes extends BasicRes{

	private Integer menuId;
	private Integer storesId;
    private Integer salesVolume;
    private String statsType;
    private List<SalesStats> salesList; // 用於返回排行榜
    private List<SalesLeaderboardProjection> salesDetailList;
	public Integer getMenuId() {
		return menuId;
	}
	public void setMenuId(Integer menuId) {
		this.menuId = menuId;
	}
	
	
	public Integer getStoresId() {
		return storesId;
	}
	public void setStoresId(Integer storesId) {
		this.storesId = storesId;
	}
	public Integer getSalesVolume() {
		return salesVolume;
	}
	public void setSalesVolume(Integer salesVolume) {
		this.salesVolume = salesVolume;
	}
	public String getStatsType() {
		return statsType;
	}
	public void setStatsType(String statsType) {
		this.statsType = statsType;
	}
	public List<SalesStats> getSalesList() {
		return salesList;
	}
	public void setSalesList(List<SalesStats> salesList) {
		this.salesList = salesList;
	}
	public List<SalesLeaderboardProjection> getSalesDetailList() {
		return salesDetailList;
	}
	public void setSalesDetailList(List<SalesLeaderboardProjection> salesDetailList) {
		this.salesDetailList = salesDetailList;
	}
	public SalesStatsRes() {
		super();
	}
	public SalesStatsRes(int code, String message) {
		super(code, message);
	}
	
    
    
}
