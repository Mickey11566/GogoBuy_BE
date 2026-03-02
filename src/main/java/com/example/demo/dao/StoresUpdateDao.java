package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Stores;

import jakarta.transaction.Transactional;

@Repository
public interface StoresUpdateDao extends JpaRepository<Stores, Integer> {

	// 檢查電話是否被其他店家使用 (排除自己)
	@Query(value = "SELECT COUNT(*) FROM stores WHERE phone = ?1 AND id != ?2 AND is_deleted = false", nativeQuery = true)
	public int countByPhoneExcludeSelf(String phone, int storeId);

	// 店家資訊更新
	@Modifying
	@Transactional
	@Query(value = "UPDATE stores SET name = ?2, phone = ?3, address = ?4, category = ?5, "//
			+ "type = ?6, memo = ?7, image = ?8, fee_description = ?9, is_public = ?10, lng = ?11, lat = ?12 "//
			+ "WHERE id = ?1", nativeQuery = true)
	public int updateStore(int id, String name, String phone, String address, String category, String type, String memo,
			String image, String fee_description, boolean is_public, double lng, double lat);

	// 店家公開狀態更新
	@Modifying
	@Transactional
	@Query(value = "UPDATE stores SET is_public = ?2 WHERE id = ?1", nativeQuery = true)
	public int updatePublishStatus(int id, boolean isPublic);

	// 各項目的細節更新 (單項修改)

	// 更新營業時間
	@Modifying
	@Transactional
	@Query(value = "UPDATE store_operating_hours SET open_time = ?2, close_time = ?3 WHERE id = ?1", nativeQuery = true)
	public int updateOperatingHours(int id, String openTime, String closeTime);

	// 更新菜單品項
	@Modifying
	@Transactional
	@Query(value = "UPDATE menu SET name = ?2, base_price = ?3, unusual = ?4, is_available = ?5 WHERE id = ?1", nativeQuery = true)
	public int updateMenu(int menuId, String name, int basePrice, String unusual, boolean available);

	// 更新品項類別群組 (例如: 尺寸級距)
	@Modifying
	@Transactional
	@Query(value = "UPDATE menu_categories SET name = ?2, price_level = ?3 WHERE id = ?1", nativeQuery = true)
	public int updateMenuCategory(int categoryId, String name, String priceLevel);

	// 更新選項群組 (例如: 甜度、加料)
	@Modifying
	@Transactional
	@Query(value = "UPDATE product_option_groups SET name = ?2, is_required = ?3, max_selection = ?4 WHERE id = ?1", nativeQuery = true)
	public int updateOptionGroup(int groupId, String name, boolean isRequired, int maxSelection);

	// 更新具體選項內容 (例如: 珍珠 +10元)
	@Modifying
	@Transactional
	@Query(value = "UPDATE product_option_items SET name = ?2, extra_price = ?3 WHERE id = ?1", nativeQuery = true)
	public int updateOptionItem(int itemId, String name, int extraPrice);

	// 刪除功能 (砍相關key表時使用(砍店)) ---

	// 根據店家id刪除營業時間
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM store_operating_hours WHERE stores_id = ?1", nativeQuery = true)
	public void deleteOperatingHoursByStoreId(int storeId);

	// 根據店家id刪除菜單品項 (就算有訂單也能刪，由上層先軟刪再硬刪)
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM menu WHERE stores_id = ?1 AND id NOT IN (SELECT DISTINCT menu_id FROM orders WHERE is_deleted = false)", nativeQuery = true)
	public void deleteMenuByStoreId(int storeId);

	// 標記有訂單的菜單品項為不可用 (is_available = false，避免 FK 衝突，並阻止新团購選用)
	@Modifying
	@Transactional
	@Query(value = "UPDATE menu SET is_available = false WHERE stores_id = ?1 AND id IN (SELECT DISTINCT menu_id FROM orders WHERE is_deleted = false)", nativeQuery = true)
	public void markOrderedMenuItemsUnavailable(int storeId);

	// 根據店家id刪除菜單分類 (如果分類內已無品項則刪除，否則保留)
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM menu_categories WHERE stores_id = ?1 AND id NOT IN (SELECT DISTINCT category_id FROM menu WHERE stores_id = ?1)", nativeQuery = true)
	public void deleteMenuCategoriesByStoreId(int storeId);

	// 根據店家id刪除群組選項
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM product_option_groups WHERE stores_id = ?1", nativeQuery = true)
	public void deleteOptionGroupsByStoreId(int storeId);

	// 根據店家id刪除群組選項
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM product_option_items WHERE group_id IN "
			+ "(SELECT id FROM product_option_groups WHERE stores_id = ?1)", nativeQuery = true)
	public void deleteOptionItemsByStoreId(int storeId);

	// 根據群組id刪除營業時間
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM product_option_items WHERE group_id = ?1", nativeQuery = true)
	public void deleteOptionItemsByGroupId(int groupId);

	// 店家資料刪除
	@Modifying
	@Transactional
	@Query(value = "DELETE FROM stores WHERE id = ?1", nativeQuery = true)
	public int deleteStoreById(int storeId);

	// 店家軟刪除
	@Modifying
	@Transactional
	@Query(value = "UPDATE stores SET is_deleted = true WHERE id = ?1", nativeQuery = true)
	public int softDeleteStoreById(int storeId);
}
