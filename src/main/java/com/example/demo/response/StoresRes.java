package com.example.demo.response;

import java.util.List;
import java.util.Map;

import com.example.demo.entity.Stores;
import com.example.demo.vo.FeeDescriptionVo;
import com.example.demo.vo.MenuCategoriesVo;
import com.example.demo.vo.MenuVo;
import com.example.demo.vo.ProductOptionGroupsVo;
import com.example.demo.vo.StoreOperatingHoursVo;

public class StoresRes extends BasicRes {

	// 舊功能專用 (getAllStores, getStoreById, getStoresByName)
	private List<Stores> storeList;

	// 新功能專用 (用於附近店家的 Projection)
	private List<?> data;
	
	//營業中篩選用	
	private List<Map<String, Object>> storeOperatingList;

	private List<StoreOperatingHoursVo> operatingHoursVoList;
	private List<MenuVo> menuVoList;
	private List<MenuCategoriesVo> menuCategoriesVoList;
	private List<ProductOptionGroupsVo> productOptionGroupsVoList;
	private List<FeeDescriptionVo> feeDescriptionVoList;

	// --- 建構子 (Constructors) ---

	public StoresRes() {
		super();
	}

	public StoresRes(int code, String message) {
		super(code, message);
	}

	// 供舊有的 Service 方法使用 (原本接收 List<Stores> 的建構子)
	public StoresRes(int code, String message, List<Stores> storeList) {
		super(code, message);
		this.storeList = storeList;
	}

	// 新增供 getNearbyStores 傳入 Projection 列表使用
	// 增加一個 dummy 參數 boolean isProjection 是為了區分建構子簽章
	public StoresRes(int code, String message, List<?> data, boolean isProjection) {
		super(code, message);
		this.data = data;
	}

	// 供詳細資料 getStoreById 使用的完整建構子
	public StoresRes(int code, String message, List<Stores> storeList, List<StoreOperatingHoursVo> operatingHoursVoList,
			List<MenuVo> menuVoList, List<MenuCategoriesVo> menuCategoriesVoList,
			List<ProductOptionGroupsVo> productOptionGroupsVoList) {
		super(code, message);
		this.storeList = storeList;
		this.operatingHoursVoList = operatingHoursVoList;
		this.menuVoList = menuVoList;
		this.menuCategoriesVoList = menuCategoriesVoList;
		this.productOptionGroupsVoList = productOptionGroupsVoList;
	}


	public List<Stores> getStoreList() {
		return storeList;
	}

	public void setStoreList(List<Stores> storeList) {
		this.storeList = storeList;
	}

	public List<?> getData() {
		return data;
	}

	public void setData(List<?> data) {
		this.data = data;
	}

	public List<StoreOperatingHoursVo> getOperatingHoursVoList() {
		return operatingHoursVoList;
	}

	public void setOperatingHoursVoList(List<StoreOperatingHoursVo> list) {
		this.operatingHoursVoList = list;
	}

	public List<MenuVo> getMenuVoList() {
		return menuVoList;
	}

	public void setMenuVoList(List<MenuVo> list) {
		this.menuVoList = list;
	}

	public List<MenuCategoriesVo> getMenuCategoriesVoList() {
		return menuCategoriesVoList;
	}

	public void setMenuCategoriesVoList(List<MenuCategoriesVo> list) {
		this.menuCategoriesVoList = list;
	}

	public List<ProductOptionGroupsVo> getProductOptionGroupsVoList() {
		return productOptionGroupsVoList;
	}

	public void setProductOptionGroupsVoList(List<ProductOptionGroupsVo> list) {
		this.productOptionGroupsVoList = list;
	}

	public List<FeeDescriptionVo> getFeeDescriptionVoList() {
		return feeDescriptionVoList;
	}

	public void setFeeDescriptionVoList(List<FeeDescriptionVo> list) {
		this.feeDescriptionVoList = list;
	}

	public List<Map<String, Object>> getStoreOperatingList() {
		return storeOperatingList;
	}

	public void setStoreOperatingList(List<Map<String, Object>> storeOperatingList) {
		this.storeOperatingList = storeOperatingList;
	}
	
	
}