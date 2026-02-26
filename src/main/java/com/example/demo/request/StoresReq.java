package com.example.demo.request;


import java.util.List;

import com.example.demo.constants.ValidationMsg;
import com.example.demo.vo.FeeDescriptionVo;
import com.example.demo.vo.MenuCategoriesVo;
import com.example.demo.vo.ProductOptionGroupsVo;
import com.example.demo.vo.ProductOptionItemsVo;
import com.example.demo.vo.StoreOperatingHoursVo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

public class StoresReq {
	
	public interface CreateStoreGroup {}  // 用於新增店家
	public interface SearchStoreGroup {}  // 用於搜尋營業店家

	@NotBlank(message = ValidationMsg.STORE_NAME_EMPTY, groups = CreateStoreGroup.class)
	private String storesname;

//	@NotBlank(message = ValidationMsg.PHONE_EMPTY)
	private String phone;
	
//	@NotBlank(message = ValidationMsg.ADDRESS_EMPTY)
	private String address;

	@NotBlank(message = ValidationMsg.CATEGORY_EMPTY, groups = CreateStoreGroup.class)
	private String category;

	@NotBlank(message = ValidationMsg.TYPE_EMPTY, groups = CreateStoreGroup.class)
	private String type;

	private String memo;

	private String image;

	private List<FeeDescriptionVo> fee_description;

	private boolean publish;
	
	private String createdBy;
	
	private Double lng;
	
	private Double lat;
	
	@Valid
	private List<StoreOperatingHoursVo>operatingHoursVoList;
//	@Valid
//	private List<MenuVo> menuVoList;
	@Valid
	private List<MenuCategoriesVo> menuCategoriesVoList;
	@Valid
	private List<ProductOptionGroupsVo> productOptionGroupsVoList;
	@Valid
	private List<ProductOptionItemsVo> productOptionItemsVoList;
	@Valid
	private List<Integer> filteredStoreIds;

	public String getStoresname() {
		return storesname;
	}

	public void setStoresname(String storesname) {
		this.storesname = storesname;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public List<FeeDescriptionVo> getFee_description() {
		return fee_description;
	}

	public void setFee_description(List<FeeDescriptionVo> fee_description) {
		this.fee_description = fee_description;
	}

	public boolean isPublish() {
		return publish;
	}

	public void setPublish(boolean publish) {
		this.publish = publish;
	}

	public List<StoreOperatingHoursVo> getOperatingHoursVoList() {
		return operatingHoursVoList;
	}

	public void setOperatingHoursVoList(List<StoreOperatingHoursVo> operatingHoursVoList) {
		this.operatingHoursVoList = operatingHoursVoList;
	}

//	public List<MenuVo> getMenuVoList() {
//		return menuVoList;
//	}
//
//	public void setMenuVoList(List<MenuVo> menuVoList) {
//		this.menuVoList = menuVoList;
//	}

	public List<MenuCategoriesVo> getMenuCategoriesVoList() {
		return menuCategoriesVoList;
	}

	public void setMenuCategoriesVoList(List<MenuCategoriesVo> menuCategoriesVoList) {
		this.menuCategoriesVoList = menuCategoriesVoList;
	}

	public List<ProductOptionGroupsVo> getProductOptionGroupsVoList() {
		return productOptionGroupsVoList;
	}

	public void setProductOptionGroupsVoList(List<ProductOptionGroupsVo> productOptionGroupsVoList) {
		this.productOptionGroupsVoList = productOptionGroupsVoList;
	}

	public List<ProductOptionItemsVo> getProductOptionItemsVoList() {
		return productOptionItemsVoList;
	}

	public void setProductOptionItemsVoList(List<ProductOptionItemsVo> productOptionItemsVoList) {
		this.productOptionItemsVoList = productOptionItemsVoList;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public List<Integer> getFilteredStoreIds() {
		return filteredStoreIds;
	}

	public void setFilteredStoreIds(List<Integer> filteredStoreIds) {
		this.filteredStoreIds = filteredStoreIds;
	}

	

	
}
