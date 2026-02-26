package com.example.demo.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Stores;

import jakarta.transaction.Transactional;

@Repository
public interface StoresCreateDao extends JpaRepository<Stores, Integer> {

	// 檢查是否有電話(已經存在)
	@Query(value = "SELECT COUNT(*) FROM stores WHERE phone = ?1 AND is_deleted = false AND is_public = true", nativeQuery = true)
	public int existsByPhone(String phone);

	// 新增店家
	@Modifying
	@Transactional
	@Query(value = "insert into stores(name, phone, address, category, type," //
			+ "memo, image, fee_description, is_public, created_by) " //
			+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10)", nativeQuery = true)
	public int addStore(String name, String phone, String address, //
			String category, String type, String memo, //
			String image, String fee_description, //
			boolean is_public, String createdBy);

	// 避免同時建利店家時id衝突
	@Query(value = "SELECT LAST_INSERT_ID()", nativeQuery = true)
	public int getLastInsertId();

	// 新增時刻
	@Modifying
	@Transactional
	@Query(value = "insert into store_operating_hours(stores_id, week, open_time, close_time )"//
			+ " values (?1, ?2, ?3, ?4)", nativeQuery = true)
	public int addOperatingHours(int storesId, int week, String openTime, String closeTime);

	// 新增菜單
	@Modifying
	@Transactional
	@Query(value = "insert into menu(stores_id, category_id, name, description,"
			+ " base_price, image, is_available, unusual)"//
			+ " values (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)", nativeQuery = true)
	public int addMenu(int storesId, int categoryId, String name, //
			String description, int base_price, //
			String image, boolean available, String unusual);

	// 新增品項群組
	@Modifying
	@Transactional
	@Query(value = "insert into menu_categories(stores_id, name, price_level)"//
			+ " values (?1, ?2, ?3)", nativeQuery = true)
	public int addCategory(int storesId, String name, String priceLevel);

	// 新增選項群組
	@Modifying
	@Transactional
	@Query(value = "insert into product_option_groups(stores_id, name, is_required, max_selection )"//
			+ " values (?1, ?2, ?3, ?4)", nativeQuery = true)
	public int addOptionGroups(int storesId, String name, boolean required, int maxSelection);

	// 新增選項
	@Modifying
	@Transactional
	@Query(value = "insert into product_option_items(group_id, name, extra_price)"//
			+ " values (?1, ?2, ?3)", nativeQuery = true)
	public int addOptionItems(int groupId, String name, int extraPrice);

}
